package no.nav.tiltakspenger.saksbehandling.service.sak

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.service.SøknadService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.FantIkkeFnr
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling.HarAlleredeStartetBehandlingen
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling.HarIkkeTilgangTilPerson
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.opprettBehandlingMapper

class SakServiceImpl(
    private val sakRepo: SakRepo,
    private val saksoversiktRepo: SaksoversiktRepo,
    private val søknadService: SøknadService,
    private val personGateway: PersonGateway,
    private val skjermingGateway: SkjermingGateway,
    private val sessionFactory: SessionFactory,
    private val tiltakGateway: TiltakGateway,
    private val statistikkSakRepo: StatistikkSakRepo,
    private val gitHash: String,
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
        sakRepo.hentForSøknadId(søknadId)?.also {
            return HarAlleredeStartetBehandlingen(it.førstegangsbehandling.id).left()
        }
        val søknad = søknadService.hentSøknad(søknadId)
        val fnr = søknad.fnr
        if (sakRepo.hentForFnr(fnr).isNotEmpty()) {
            throw IllegalStateException("Vi støtter ikke flere saker per søker i piloten. søknadId: $søknadId")
        }
        val sakPersonopplysninger =
            SakPersonopplysninger(
                liste = runBlocking { personGateway.hentPerson(fnr) },
            ).let { runBlocking { it.medSkjermingFra(lagListeMedSkjerming(it.liste)) } }
                .also {
                    // TODO pre-mvp jah: Denne sjekken bør gjøres av domenekoden, ikke servicen.
                    if (!it.harTilgang(saksbehandler)) {
                        sikkerlogg.info {
                            "Saksbehandler ${saksbehandler.navIdent} " +
                                "med roller ${saksbehandler.roller}, har ikke tilgang til person : ${it.søker()}"
                        }
                        return HarIkkeTilgangTilPerson.left()
                    }
                }

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

        val statistikk =
            opprettBehandlingMapper(
                sak = sak.sakDetaljer,
                behandling = sak.førstegangsbehandling,
                gjelderKode6 = sakPersonopplysninger.erSøkerStrengtFortrolig(),
                versjon = gitHash,
            )

        sessionFactory.withTransactionContext { tx ->
            sakRepo.lagre(sak, tx)
            statistikkSakRepo.lagre(statistikk, tx)
        }

        return sak.right()
    }

    override fun hentForFørstegangsbehandlingId(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
    ): Sak {
        val sak = sakRepo.hentForFørstegangsbehandlingId(behandlingId) ?: throw IkkeFunnetException("Sak ikke funnet")
        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
        }
        return sak
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

    data object FantIkkeFnr

    override fun hentForFnr(
        fnr: Fnr,
        saksbehandler: Saksbehandler,
    ): Either<FantIkkeFnr, Sak> {
        val saker = sakRepo.hentForFnr(fnr)
        if (saker.saker.isEmpty()) FantIkkeFnr.left()
        if (saker.size > 1) throw IllegalStateException("Vi støtter ikke flere saker per søker i piloten. fnr: $fnr")

        val sak = saker.single()
        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak på fnr $fnr")
        }

        return sak.right()
    }

    override fun hentFnrForSakId(sakId: SakId): Fnr? = sakRepo.hentFnrForSakId(sakId)

    override fun hentForSakId(
        sakId: SakId,
        saksbehandler: Saksbehandler,
    ): Sak? {
        val sak = sakRepo.hentForSakId(sakId) ?: return null
        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak ${sak.id}")
        }
        return sak
    }

    override fun hentSaksoversikt(saksbehandler: Saksbehandler): Saksoversikt {
        require(saksbehandler.isSaksbehandler())
        // TODO pre-mvp tilgang jah: Legg på sjekk på kode 6/7/skjermet. Filtrerer vi bare bort de som er skjermet?
        return saksoversiktRepo.hentAlle()
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
