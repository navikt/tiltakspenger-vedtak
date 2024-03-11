package no.nav.tiltakspenger.vedtak.service.sak

import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.domene.personopplysninger.barnMedIdent
import no.nav.tiltakspenger.domene.personopplysninger.erLik
import no.nav.tiltakspenger.domene.personopplysninger.søker
import no.nav.tiltakspenger.domene.personopplysninger.søkerMedIdent
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vedtak.innsending.Skjerming
import no.nav.tiltakspenger.vedtak.innsending.tolkere.AlderTolker
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SakServiceImpl(
    val sakRepo: SakRepo,
    val behandlingRepo: BehandlingRepo,
    val behandlingService: BehandlingService,
) : SakService {
    override fun motta(søknad: Søknad): Sak {
        val sak: Sak =
            sakRepo.hentForIdentMedPeriode(
                fnr = søknad.personopplysninger.ident,
                periode = søknad.vurderingsperiode(),
            ).singleOrNull() ?: Sak.lagSak(
                søknad = søknad,
                saksnummerGenerator = SaksnummerGenerator(),
            )

        val håndtertSak = sak.håndter(søknad = søknad)

        return sakRepo.lagre(håndtertSak)
    }

    override fun mottaPersonopplysninger(journalpostId: String, personopplysninger: List<Personopplysninger>): Sak? {
        val sak = sakRepo.hentForJournalpostId(journalpostId)
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere personopplysninger")

        val personopplysningerMedSkjerming = personopplysninger.map {
            when (it) {
                is PersonopplysningerBarnMedIdent -> it.copy(
                    skjermet = sak.personopplysninger.barnMedIdent()
                        .firstOrNull { barn -> barn.ident == it.ident }?.skjermet,
                )

                is PersonopplysningerBarnUtenIdent -> it
                is PersonopplysningerSøker -> it.copy(
                    skjermet = sak.personopplysninger.søkerMedIdent(it.ident)?.skjermet,
                )
            }
        }

        val fdato = personopplysninger.søker().fødselsdato
        sak.behandlinger.filterIsInstance<Søknadsbehandling>().forEach { behandling ->
            AlderTolker.tolkeData(fdato, sak.periode).forEach {
                behandlingService.leggTilSaksopplysning(behandling.id, it)
            }
        }

        // Hvis personopplysninger ikke er endret trenger vi ikke oppdatere
        if (personopplysningerMedSkjerming.erLik(sak.personopplysninger)) return sak

        val oppdatertSak = sakRepo.hentForJournalpostId(journalpostId)?.copy(
            personopplysninger = personopplysningerMedSkjerming,
        )
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere personopplysninger")

        sakRepo.lagre(oppdatertSak)
        return sakRepo.hent(oppdatertSak.id)
    }

    override fun mottaSkjerming(journalpostId: String, skjerming: Skjerming): Sak {
        val sak = sakRepo.hentForJournalpostId(journalpostId)
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere skjerming")

        val oppdatertSak = sak.copy(
            personopplysninger = sak.personopplysninger.map {
                when (it) {
                    is PersonopplysningerBarnMedIdent -> it.copy(
                        skjermet = skjerming
                            .barn.firstOrNull { barn -> barn.ident == it.ident }?.skjerming,
                    )

                    is PersonopplysningerBarnUtenIdent -> it
                    is PersonopplysningerSøker -> it.copy(
                        skjermet = skjerming.søker.skjerming,
                    )
                }
            },
        )
        return sakRepo.lagre(oppdatertSak)
    }

    override fun henteMedBehandlingsId(behandlingId: BehandlingId): Sak? {
        val behandling = behandlingRepo.hent(behandlingId) ?: return null
        return sakRepo.hent(behandling.sakId)
    }
}
