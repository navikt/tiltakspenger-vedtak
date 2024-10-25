package no.nav.tiltakspenger.saksbehandling.service.sak

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering
import no.nav.tiltakspenger.libs.person.harStrengtFortroligAdresse
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.EnkelPerson
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.service.SøknadService
import no.nav.tiltakspenger.saksbehandling.service.person.KunneIkkeHenteEnkelPerson
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.opprettBehandlingMapper

class SakServiceImpl(
    private val sakRepo: SakRepo,
    private val saksoversiktRepo: SaksoversiktRepo,
    private val søknadService: SøknadService,
    private val personService: PersonService,
    private val sessionFactory: SessionFactory,
    private val tiltakGateway: TiltakGateway,
    private val statistikkSakRepo: StatistikkSakRepo,
    private val tilgangsstyringService: TilgangsstyringService,
    private val gitHash: String,
) : SakService {
    val logger = KotlinLogging.logger { }

    override suspend fun startFørstegangsbehandling(
        søknadId: SøknadId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Either<KanIkkeStarteFørstegangsbehandling, Sak> {
        val fnr = personService.hentFnrForSøknadId(søknadId)

        sjekkTilgangTilSøknad(fnr, søknadId, saksbehandler, correlationId)

        sakRepo.hentForSøknadId(søknadId)?.also {
            return KanIkkeStarteFørstegangsbehandling.HarAlleredeStartetBehandlingen(it.førstegangsbehandling.id).left()
        }
        val søknad = søknadService.hentSøknad(søknadId)

        if (sakRepo.hentForFnr(fnr).isNotEmpty()) {
            throw IllegalStateException("Vi støtter ikke flere saker per søker i piloten. søknadId: $søknadId")
        }

        val registrerteTiltak = runBlocking { tiltakGateway.hentTiltak(fnr) }
        if (registrerteTiltak.isEmpty()) {
            return KanIkkeStarteFørstegangsbehandling.OppretteBehandling(
                KanIkkeOppretteBehandling.FantIkkeTiltak,
            ).left()
        }

        val personopplysninger = personService.hentPersonopplysninger(fnr)
        val adressebeskyttelseGradering: List<AdressebeskyttelseGradering>? =
            tilgangsstyringService.adressebeskyttelseEnkel(fnr)
                .getOrElse {
                    throw IllegalArgumentException(
                        "Kunne ikke hente adressebeskyttelsegradering for person. SøknadId: $søknadId",
                    )
                }
        require(adressebeskyttelseGradering != null) { "Fant ikke adressebeskyttelse for person. SøknadId: $søknadId" }

        val sak =
            Sak
                .lagSak(
                    saksnummer = sakRepo.hentNesteSaksnummer(),
                    fødselsdato = personopplysninger.fødselsdato,
                    søknad = søknad,
                    saksbehandler = saksbehandler,
                    registrerteTiltak = registrerteTiltak,
                ).getOrElse { return KanIkkeStarteFørstegangsbehandling.OppretteBehandling(it).left() }
        val statistikk =
            opprettBehandlingMapper(
                sak = sak.hentTynnSak(),
                behandling = sak.førstegangsbehandling,
                gjelderKode6 = adressebeskyttelseGradering.harStrengtFortroligAdresse(),
                versjon = gitHash,
            )

        sessionFactory.withTransactionContext { tx ->
            sakRepo.lagre(sak, tx)
            statistikkSakRepo.lagre(statistikk, tx)
        }

        return sak.right()
    }

    override suspend fun hentForFørstegangsbehandlingId(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Sak {
        val sak = sakRepo.hentForFørstegangsbehandlingId(behandlingId) ?: throw IkkeFunnetException("Sak ikke funnet")

        sjekkTilgangTilSak(sak.id, saksbehandler, correlationId)

        return sak
    }

    override suspend fun hentForSaksnummer(
        saksnummer: Saksnummer,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Sak {
        val sak =
            sakRepo.hentForSaksnummer(saksnummer)
                ?: throw IkkeFunnetException("Fant ikke sak med saksnummer $saksnummer")
        sjekkTilgangTilSak(sak.id, saksbehandler, correlationId)

        return sak
    }

    data object FantIkkeSakForFnr

    override suspend fun hentForFnr(
        fnr: Fnr,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Either<FantIkkeSakForFnr, Sak> {
        val saker = sakRepo.hentForFnr(fnr)
        if (saker.saker.isEmpty()) return FantIkkeSakForFnr.left()
        if (saker.size > 1) throw IllegalStateException("Vi støtter ikke flere saker per søker i piloten.")

        val sak = saker.single()
        sjekkTilgangTilSak(sak.id, saksbehandler, correlationId)

        return sak.right()
    }

    override fun hentFnrForSakId(sakId: SakId): Fnr? = sakRepo.hentFnrForSakId(sakId)

    override suspend fun hentForSakId(
        sakId: SakId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Sak? {
        sjekkTilgangTilSak(sakId, saksbehandler, correlationId)

        val sak = sakRepo.hentForSakId(sakId) ?: return null

        return sak
    }

    override suspend fun hentSaksoversikt(
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Saksoversikt {
        require(saksbehandler.isSaksbehandler() || saksbehandler.isBeslutter()) { "Saksbehandler ${saksbehandler.navIdent} må ha rollen SAKSBEHANDLER eller BESLUTTER" }
        val saksoversikt: Saksoversikt = saksoversiktRepo.hentAlle()
        if (saksoversikt.isEmpty()) return saksoversikt
        val tilganger = tilgangsstyringService.harTilgangTilPersoner(
            fnrListe = saksoversikt.map { it.fnr }.toNonEmptyListOrNull()!!,
            roller = saksbehandler.roller,
            correlationId = correlationId,
        ).getOrElse { throw IllegalStateException("Feil ved henting av tilganger") }
        return saksoversikt.filter { tilganger[it.fnr] == true }
    }

    override suspend fun hentEnkelPersonForSakId(sakId: SakId): Either<KunneIkkeHenteEnkelPerson, EnkelPerson> {
        val fnr = sakRepo.hentFnrForSakId(sakId) ?: return KunneIkkeHenteEnkelPerson.FantIkkeSakId.left()
        return personService.hentEnkelPersonFnr(fnr)
    }

    private suspend fun sjekkTilgangTilSak(sakId: SakId, saksbehandler: Saksbehandler, correlationId: CorrelationId) {
        val fnr = personService.hentFnrForSakId(sakId)
        tilgangsstyringService
            .harTilgangTilPerson(
                fnr = fnr,
                roller = saksbehandler.roller,
                correlationId = correlationId,
            )
            .onLeft { throw IkkeFunnetException("Feil ved sjekk av tilgang til person. SakId: $sakId. CorrelationId: $correlationId") }
            .onRight { if (!it) throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til person") }
    }

    private suspend fun sjekkTilgangTilSøknad(
        fnr: Fnr,
        søknadId: SøknadId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ) {
        tilgangsstyringService
            .harTilgangTilPerson(
                fnr = fnr,
                roller = saksbehandler.roller,
                correlationId = correlationId,
            )
            .onLeft { throw IkkeFunnetException("Feil ved sjekk av tilgang til person. SøknadId: $søknadId. CorrelationId: $correlationId") }
            .onRight { if (!it) throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til person") }
    }
}
