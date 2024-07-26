package no.nav.tiltakspenger.saksbehandling.service.sak

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.søker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.AlderTolker
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SøkerRepository
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.opprettBehandlingMapper

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SakServiceImpl(
    val sakRepo: SakRepo,
    val behandlingRepo: BehandlingRepo,
    val søkerRepository: SøkerRepository,
    val behandlingService: BehandlingService,
    val personGateway: PersonGateway,
    val skjermingGateway: SkjermingGateway,
    val statistikkSakRepo: StatistikkSakRepo,
) : SakService {
    override fun motta(søknad: Søknad): Sak {
        val ident = søknad.personopplysninger.ident
        val sakPersonopplysninger = SakPersonopplysninger(
            liste = runBlocking { personGateway.hentPerson(ident) },
        ).let { runBlocking { it.medSkjermingFra(lagListeMedSkjerming(it.liste)) } }
        val sak: Sak =
            (
                sakRepo.hentForIdent(
                    fnr = ident,
                ).singleOrNull() ?: Sak.lagSak(
                    søknad = søknad,
                    saksnummer = sakRepo.hentNesteSaksnummer(),
                    sakPersonopplysninger = sakPersonopplysninger,
                )
                ).håndter(søknad = søknad)

        return sakRepo.lagre(sak).also { lagretSak ->
            val statistikk = opprettBehandlingMapper(
                sak = lagretSak.sakDetaljer,
                behandling = lagretSak.behandlinger.single { behandling ->
                    behandling.søknad().id == søknad.id
                } as Førstegangsbehandling,
            )
            statistikkSakRepo.lagre(statistikk)
            utførLegacyInnsendingFørViSletterRnR(lagretSak, sakPersonopplysninger)
        }
    }

    /** TODO jah: Skal slettes når vi tar ned RnR. */
    private fun utførLegacyInnsendingFørViSletterRnR(sak: Sak, sakPersonopplysninger: SakPersonopplysninger) {
        // Opprett eller oppdater Søker. Denne brukes for å sjekke tilgang og for å veksle inn ident i søkerId
        // slik at vi slipper å bruke ident i url'er
        val søker = søkerRepository.findByIdent(sak.ident) ?: Søker(sak.ident)
        søker.personopplysninger = sakPersonopplysninger.liste.søker()
        søkerRepository.lagre(søker)

        // Lage saksopplysninger for alder. Dette skal vel sikkert endres...
        // saksopplysningen blir lagret i basen i behandlingservicen
        sak.behandlinger.filterIsInstance<Førstegangsbehandling>().forEach { behandling ->
            AlderTolker.tolkeData(sak.personopplysninger.søker().fødselsdato, sak.periode).forEach {
                behandlingService.leggTilSaksopplysning(behandling.id, it)
            }
        }
    }

    override fun hentMedBehandlingIdOrNull(behandlingId: BehandlingId): Sak? {
        val behandling = behandlingRepo.hentOrNull(behandlingId) ?: return null
        return sakRepo.hent(behandling.sakId)
    }

    override fun hentMedBehandlingId(behandlingId: BehandlingId, saksbehandler: Saksbehandler): Sak {
        val behandling = behandlingRepo.hent(behandlingId)
        val sak = sakRepo.hent(behandling.sakId) ?: throw IkkeFunnetException("Sak ikke funnet")
        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
        }
        return sak
    }

    override fun hentForIdent(ident: String, saksbehandler: Saksbehandler): Saker {
        val saker = sakRepo.hentForIdent(ident)
        saker.forEach { sak ->
            if (!sak.personopplysninger.harTilgang(saksbehandler)) {
                throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
            }
        }

        return saker
    }

    override fun hentForSaksnummer(saksnummer: String, saksbehandler: Saksbehandler): Sak {
        val sak = sakRepo.hentForSaksnummer(saksnummer)
            ?: throw IkkeFunnetException("Fant ikke sak med saksnummer $saksnummer")
        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
        }
        return sak
    }

    private suspend fun lagListeMedSkjerming(liste: List<Personopplysninger>): Map<String, Boolean> {
        return liste
            .filterNot { it is PersonopplysningerBarnUtenIdent }
            .associate {
                when (it) {
                    is PersonopplysningerSøker -> it.ident to skjermingGateway.erSkjermetPerson(it.ident)
                    is PersonopplysningerBarnMedIdent -> it.ident to skjermingGateway.erSkjermetPerson(it.ident)
                    is PersonopplysningerBarnUtenIdent -> throw IllegalStateException("Barn uten ident skal ikke være i listen")
                }
            }
    }
}
