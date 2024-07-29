package no.nav.tiltakspenger.saksbehandling.service.sak

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.søker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.ports.SøkerRepository
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling.HarAlleredeStartetBehandlingen
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling.HarIkkeTilgangTilPerson

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SakServiceImpl(
    private val sakRepo: SakRepo,
    private val søknadRepo: SøknadRepo,
    private val behandlingRepo: BehandlingRepo,
    private val søkerRepository: SøkerRepository,
    private val behandlingService: BehandlingService,
    private val personGateway: PersonGateway,
    private val skjermingGateway: SkjermingGateway,
    private val sessionFactory: SessionFactory,
    private val tiltakGateway: TiltakGateway,
) : SakService {

    sealed interface KanIkkeStarteFørstegangsbehandling {
        data object HarIkkeTilgangTilPerson : KanIkkeStarteFørstegangsbehandling
        data object HarAlleredeStartetBehandlingen : KanIkkeStarteFørstegangsbehandling
    }

    override fun startFørstegangsbehandling(
        søknadId: SøknadId,
        saksbehandler: Saksbehandler,
    ): Either<KanIkkeStarteFørstegangsbehandling, Sak> {
        val søknad = søknadRepo.hentSøknad(søknadId)
        if (behandlingService.hentBehandlingForSøknadId(søknadId) != null) {
            return HarAlleredeStartetBehandlingen.left()
        }
        val ident = søknad.personopplysninger.ident
        if (sakRepo.hentForIdent(ident).isNotEmpty()) {
            throw IllegalStateException("Vi støtter ikke flere saker per søker i piloten. søknadId: $søknadId")
        }
        val sakPersonopplysninger = SakPersonopplysninger(
            liste = runBlocking { personGateway.hentPerson(ident) },
        ).also {
            // TODO jah: Denne sjekken bør gjøres av domenekoden, ikke servicen.
            if (!it.harTilgang(saksbehandler)) return HarIkkeTilgangTilPerson.left()
        }.let { runBlocking { it.medSkjermingFra(lagListeMedSkjerming(it.liste)) } }

        val registrerteTiltak = runBlocking { tiltakGateway.hentTiltak(ident) }
        require(registrerteTiltak.isNotEmpty()) { "Finner ingen tiltak tilknyttet brukeren" }

        val sak = Sak.lagSak(
            søknad = søknad,
            saksnummer = sakRepo.hentNesteSaksnummer(),
            sakPersonopplysninger = sakPersonopplysninger,
        ).nyFørstegangsbehandling(søknad, saksbehandler, registrerteTiltak)
        // .oppdaterLegacySaksopplysninger()

        sessionFactory.withTransactionContext { tx ->
            sakRepo.lagre(sak, tx)
            lagEllerOppdaterSøker(sak, sakPersonopplysninger, tx)
        }

        return sak.right()
    }

    /** TODO jah: Skal slettes etter vi har fjernet Søker-tabellen */
    private fun lagEllerOppdaterSøker(
        sak: Sak,
        sakPersonopplysninger: SakPersonopplysninger,
        tx: TransactionContext,
    ) {
        val søker = søkerRepository.findByIdent(sak.ident, tx) ?: Søker(sak.ident)
        søker.personopplysninger = sakPersonopplysninger.liste.søker()
        søkerRepository.lagre(søker, tx)
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
