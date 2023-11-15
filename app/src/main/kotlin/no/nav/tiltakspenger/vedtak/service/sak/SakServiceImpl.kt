package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.behandling.Personopplysninger
import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.behandling.erLik
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vedtak.innsending.Skjerming
import no.nav.tiltakspenger.vedtak.innsending.tolkere.AlderTolker
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService

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
                is Personopplysninger.BarnMedIdent -> it.copy(
                    skjermet = sak.personopplysninger.filterIsInstance<Personopplysninger.BarnMedIdent>()
                        .firstOrNull { barn -> barn.ident == it.ident }?.skjermet,
                )

                is Personopplysninger.BarnUtenIdent -> it
                is Personopplysninger.Søker -> it.copy(
                    skjermet = sak.personopplysninger.filterIsInstance<Personopplysninger.Søker>()
                        .firstOrNull { søker -> søker.ident == it.ident }?.skjermet,
                )
            }
        }

        // Hvis personopplysninger ikke er endret trenger vi ikke oppdatere
        if (personopplysningerMedSkjerming.erLik(sak.personopplysninger)) return sak

        val oppdatertSak = sak.copy(
            personopplysninger = personopplysningerMedSkjerming,
        )

        sakRepo.lagre(oppdatertSak)

        val fdato = personopplysninger.filterIsInstance<Personopplysninger.Søker>().first().fødselsdato
        oppdatertSak.behandlinger.filterIsInstance<Søknadsbehandling>().forEach { behandling ->
            AlderTolker.tolkeData(fdato, sak.periode).forEach {
                behandlingService.leggTilSaksopplysning(behandling.id, it)
            }
        }

        return sakRepo.hent(oppdatertSak.id)
    }

    override fun mottaSkjerming(journalpostId: String, skjerming: Skjerming): Sak {
        val sak = sakRepo.hentForJournalpostId(journalpostId)
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere skjerming")

        val oppdatertSak = sak.copy(
            personopplysninger = sak.personopplysninger.map {
                when (it) {
                    is Personopplysninger.BarnMedIdent -> it.copy(
                        skjermet = skjerming
                            .barn.firstOrNull { barn -> barn.ident == it.ident }?.skjerming,
                    )

                    is Personopplysninger.BarnUtenIdent -> it
                    is Personopplysninger.Søker -> it.copy(
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
