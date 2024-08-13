package no.nav.tiltakspenger.saksbehandling.service.sak

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling.HarAlleredeStartetBehandlingen
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling.HarIkkeTilgangTilPerson
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.opprettBehandlingMapper

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SakServiceImpl(
    private val sakRepo: SakRepo,
    private val søknadRepo: SøknadRepo,
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
    private val personGateway: PersonGateway,
    private val skjermingGateway: SkjermingGateway,
    private val sessionFactory: SessionFactory,
    private val tiltakGateway: TiltakGateway,
    private val statistikkSakRepo: StatistikkSakRepo,
) : SakService {
    sealed interface KanIkkeStarteFørstegangsbehandling {
        data object HarIkkeTilgangTilPerson : KanIkkeStarteFørstegangsbehandling

        data class HarAlleredeStartetBehandlingen(
            val behandlingId: BehandlingId,
        ) : KanIkkeStarteFørstegangsbehandling

        data class OppretteBehandling(
            val underliggende: KanIkkeOppretteBehandling,
        ) : KanIkkeStarteFørstegangsbehandling
    }

    override fun startFørstegangsbehandling(
        søknadId: SøknadId,
        saksbehandler: Saksbehandler,
    ): Either<KanIkkeStarteFørstegangsbehandling, Sak> {
        val søknad = søknadRepo.hentSøknad(søknadId)
        behandlingService.hentBehandlingForSøknadId(søknadId)?.also {
            return HarAlleredeStartetBehandlingen(it.id).left()
        }
        val fnr = søknad.personopplysninger.fnr
        if (sakRepo.hentForIdent(fnr).isNotEmpty()) {
            throw IllegalStateException("Vi støtter ikke flere saker per søker i piloten. søknadId: $søknadId")
        }
        val sakPersonopplysninger =
            SakPersonopplysninger(
                liste = runBlocking { personGateway.hentPerson(fnr) },
            ).also {
                // TODO jah: Denne sjekken bør gjøres av domenekoden, ikke servicen.
                if (!it.harTilgang(saksbehandler)) return HarIkkeTilgangTilPerson.left()
            }.let { runBlocking { it.medSkjermingFra(lagListeMedSkjerming(it.liste)) } }

        val registrerteTiltak = runBlocking { tiltakGateway.hentTiltak(fnr) }
        require(registrerteTiltak.isNotEmpty()) { "Finner ingen tiltak tilknyttet brukeren" }

        val sak =
            Sak
                .lagSak(
                    saksnummer = sakRepo.hentNesteSaksnummer(),
                    sakPersonopplysninger = sakPersonopplysninger,
                    søknad = søknad,
                    saksbehandler = saksbehandler,
                    registrerteTiltak = registrerteTiltak,
                ).getOrElse { return KanIkkeStarteFørstegangsbehandling.OppretteBehandling(it).left() }

        sakRepo.lagre(sak).also { lagretSak ->
            val statistikk = opprettBehandlingMapper(
                sak = lagretSak.sakDetaljer,
                behandling = lagretSak.behandlinger.single { behandling ->
                    behandling.søknad.id == søknad.id
                } as Førstegangsbehandling,
            )
            statistikkSakRepo.lagre(statistikk)
        }

        return sak.right()
    }

    override fun hentMedBehandlingIdOrNull(behandlingId: BehandlingId): Sak? {
        val behandling = behandlingRepo.hentOrNull(behandlingId) ?: return null
        return sakRepo.hent(behandling.sakId)
    }

    override fun hentMedBehandlingId(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
    ): Sak {
        val behandling = behandlingRepo.hent(behandlingId)
        val sak = sakRepo.hent(behandling.sakId) ?: throw IkkeFunnetException("Sak ikke funnet")
        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
        }
        return sak
    }

    override fun hentForIdent(
        fnr: Fnr,
        saksbehandler: Saksbehandler,
    ): Saker {
        val saker = sakRepo.hentForIdent(fnr)
        saker.forEach { sak ->
            if (!sak.personopplysninger.harTilgang(saksbehandler)) {
                throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
            }
        }

        return saker
    }

    override fun hentForSaksnummer(
        saksnummer: Saksnummer,
        saksbehandler: Saksbehandler,
    ): Sak {
        val sak =
            sakRepo.hentForSaksnummer(saksnummer)
                ?: throw IkkeFunnetException("Fant ikke sak med saksnummer $saksnummer")
        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
        }
        return sak
    }

    private suspend fun lagListeMedSkjerming(liste: List<Personopplysninger>): Map<Fnr, Boolean> =
        liste
            .filterNot { it is PersonopplysningerBarnUtenIdent }
            .associate {
                when (it) {
                    is PersonopplysningerSøker -> it.fnr to skjermingGateway.erSkjermetPerson(it.fnr)
                    is PersonopplysningerBarnMedIdent -> it.fnr to skjermingGateway.erSkjermetPerson(it.fnr)
                    is PersonopplysningerBarnUtenIdent -> throw IllegalStateException("Barn uten ident skal ikke være i listen")
                }
            }
}
