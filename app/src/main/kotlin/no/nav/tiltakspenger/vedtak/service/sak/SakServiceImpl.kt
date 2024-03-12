package no.nav.tiltakspenger.vedtak.service.sak

import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.personopplysninger.SakPersonopplysninger
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

    override fun mottaPersonopplysninger(
        journalpostId: String,
        nyePersonopplysninger: SakPersonopplysninger,
    ): Sak? {
        val sak = sakRepo.hentForJournalpostId(journalpostId)
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere personopplysninger")

        val nyePersonopplysningerMedGammelSkjerming =
            nyePersonopplysninger.medSkjermingFra(sak.personopplysninger.identerOgSkjerming())

        val fdato = nyePersonopplysninger.søker().fødselsdato
        sak.behandlinger.filterIsInstance<Førstegangsbehandling>().forEach { behandling ->
            AlderTolker.tolkeData(fdato, sak.periode).forEach {
                behandlingService.leggTilSaksopplysning(behandling.id, it)
            }
        }

        // Hvis personopplysninger ikke er endret trenger vi ikke oppdatere
        if (nyePersonopplysningerMedGammelSkjerming == sak.personopplysninger) return sak

        // TODO: Hvorfor henter vi den igjen her, vi hentet den jo på starten av funksjonen?
        val oppdatertSak = sakRepo.hentForJournalpostId(journalpostId)?.copy(
            personopplysninger = nyePersonopplysningerMedGammelSkjerming,
        )
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere personopplysninger")

        sakRepo.lagre(oppdatertSak)
        return sakRepo.hent(oppdatertSak.id)
    }

    override fun mottaSkjerming(journalpostId: String, skjerming: Skjerming): Sak {
        val sak = sakRepo.hentForJournalpostId(journalpostId)
            ?: throw IllegalStateException("Fant ikke sak med journalpostId $journalpostId. Kunne ikke oppdatere skjerming")

        val oppdatertSak = sak.copy(
            personopplysninger = sak.personopplysninger.medSkjermingFra(lagMapAvSkjerming(skjerming)),
        )
        return sakRepo.lagre(oppdatertSak)
    }

    override fun henteMedBehandlingsId(behandlingId: BehandlingId): Sak? {
        val behandling = behandlingRepo.hent(behandlingId) ?: return null
        return sakRepo.hent(behandling.sakId)
    }

    private fun lagMapAvSkjerming(skjerming: Skjerming) =
        (skjerming.barn + skjerming.søker).associate { it.ident to it.skjerming }
}
