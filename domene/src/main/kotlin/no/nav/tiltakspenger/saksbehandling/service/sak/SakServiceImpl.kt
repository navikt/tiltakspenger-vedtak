package no.nav.tiltakspenger.saksbehandling.service.sak

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.meldinger.IdentMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.innsending.domene.tolkere.AlderTolker
import no.nav.tiltakspenger.innsending.ports.InnsendingMediator
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.saksbehandling.domene.skjerming.Skjerming
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerMediator

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SakServiceImpl(
    val sakRepo: SakRepo,
    val behandlingRepo: BehandlingRepo,
    val behandlingService: BehandlingService,
    val personGateway: PersonGateway,
    val søkerMediator: SøkerMediator,
    val innsendingMediator: InnsendingMediator,
    val skjermingGateway: SkjermingGateway,
) : SakService {
    override fun motta(søknad: Søknad): Sak {
        val ident = søknad.personopplysninger.ident
        val journalpostId = søknad.journalpostId
        val erSkjermet = runBlocking { skjermingGateway.erSkjermetPerson(ident) }
        println("erSkjermet: $erSkjermet")
        val sak: Sak =
            (
                sakRepo.hentForIdentMedPeriode(
                    fnr = ident,
                    periode = søknad.vurderingsperiode(),
                ).singleOrNull() ?: Sak.lagSak(
                    søknad = søknad,
                    saksnummer = SaksnummerGenerator().genererSaknummer(sakRepo.hentNesteLøpenr()),
                    sakPersonopplysninger = SakPersonopplysninger(
                        // TODO jah: Bare for å verifisere at vi får hentet personopplysninger synkront fra PDL. De vil overskrives av Innsending i motta personopplysninger steget.
                        liste = runBlocking { personGateway.hentPerson(ident) },
                    ),
                )
                ).håndter(søknad = søknad)

        return sakRepo.lagre(sak).also {
            utførLegacyInnsendingFørViSletterRnR(søknad)
        }
    }

    /** TODO jah: Skal slettes når vi tar ned RnR. */
    private fun utførLegacyInnsendingFørViSletterRnR(søknad: Søknad) {
        val ident = søknad.personopplysninger.ident
        val journalpostId = søknad.journalpostId
        // Flyttet fra SøknadRoutes.kt - Lager hendelse og trigger Innending innhenting
        val søknadMottattHendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = journalpostId,
            søknad = søknad,
        )

        SECURELOG.info { "Innsending/RnR: Mottatt søknad og lager hendelse : $søknadMottattHendelse" }
        innsendingMediator.håndter(søknadMottattHendelse)

        // Lager hendelse og trigger oppretelse av Søker hvis den ikke finnes
        val identMottattHendelse = IdentMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = ident,
        )
        søkerMediator.håndter(identMottattHendelse)
        // Kommentar jah: Tror jeg gir opp og fjerne RnR gradvis.
//        val personopplysningerMottattHendelse = PersonopplysningerMottattHendelse(
//            aktivitetslogg = Aktivitetslogg(),
//            journalpostId = søknad.journalpostId,
//            ident = ident,
//            personopplysninger = sak.personopplysninger.liste,
//            tidsstempelPersonopplysningerInnhentet = LocalDateTime.now(),
//        )
//        søkerMediator.håndter(personopplysningerMottattHendelse)
//        innsendingMediator.håndter(personopplysningerMottattHendelse)
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

    override fun hentForIdent(ident: String, saksbehandler: Saksbehandler): List<Sak> {
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

    override fun resettLøpenr() {
        sakRepo.resetLøpenummer()
    }

    private fun lagMapAvSkjerming(skjerming: Skjerming) =
        (skjerming.barn + skjerming.søker).associate { it.ident to it.skjerming }
}
