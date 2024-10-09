package no.nav.tiltakspenger.saksbehandling.service.behandling

import arrow.core.getOrElse
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering
import no.nav.tiltakspenger.libs.person.harStrengtFortroligAdresse
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.meldekort.domene.opprettFørsteMeldekortForEnSak
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attesteringsstatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.opprettVedtak
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.iverksettBehandlingMapper
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.stønadStatistikkMapper
import java.time.LocalDate

class BehandlingServiceImpl(
    private val førstegangsbehandlingRepo: BehandlingRepo,
    private val rammevedtakRepo: RammevedtakRepo,
    private val meldekortRepo: MeldekortRepo,
    private val sakRepo: SakRepo,
    private val sessionFactory: SessionFactory,
    private val statistikkSakRepo: StatistikkSakRepo,
    private val statistikkStønadRepo: StatistikkStønadRepo,
    private val tilgangsstyringService: TilgangsstyringService,
    private val personService: PersonService,
    private val gitHash: String,
) : BehandlingService {
    val logger = KotlinLogging.logger { }

    override fun hentBehandling(
        behandlingId: BehandlingId,
        sessionContext: SessionContext?,
    ): Behandling = førstegangsbehandlingRepo.hent(behandlingId, sessionContext)

    override suspend fun hentBehandling(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
        sessionContext: SessionContext?,
    ): Behandling {
        sjekkTilgang(behandlingId, saksbehandler, correlationId)

        val behandling = hentBehandling(behandlingId, sessionContext)
        return behandling
    }

    override suspend fun sendTilBeslutter(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ) {
        val behandling = hentBehandling(behandlingId, saksbehandler, correlationId).tilBeslutning(saksbehandler)
        førstegangsbehandlingRepo.lagre(behandling)
    }

    override suspend fun sendTilbakeTilSaksbehandler(
        behandlingId: BehandlingId,
        beslutter: Saksbehandler,
        begrunnelse: String,
        correlationId: CorrelationId,
    ) {
        val attestering =
            Attestering(
                status = Attesteringsstatus.SENDT_TILBAKE,
                begrunnelse = begrunnelse,
                beslutter = beslutter.navIdent,
            )

        val behandling = hentBehandling(behandlingId, beslutter, correlationId).sendTilbake(beslutter, attestering)

        sessionFactory.withTransactionContext { tx ->
            førstegangsbehandlingRepo.lagre(behandling, tx)
        }
    }

    override suspend fun iverksett(
        behandlingId: BehandlingId,
        beslutter: Saksbehandler,
        correlationId: CorrelationId,
    ) {
        val behandling = hentBehandling(behandlingId, beslutter, correlationId) as Førstegangsbehandling
        val sak =
            sakRepo.hentDetaljerForSakId(behandling.sakId)
                ?: throw IllegalStateException("Iverksett finner ikke sak ${behandling.sakId}")
        val attestering =
            Attestering(
                status = Attesteringsstatus.GODKJENT,
                begrunnelse = "",
                beslutter = beslutter.navIdent,
            )
        val iverksattBehandling = behandling.iverksett(beslutter, attestering)

        val vedtak = iverksattBehandling.opprettVedtak()

        val fnr = personService.hentFnrForBehandlingId(behandlingId)
        val adressebeskyttelseGradering: List<AdressebeskyttelseGradering>? =
            tilgangsstyringService.adressebeskyttelseEnkel(fnr)
                .getOrElse {
                    throw IllegalArgumentException(
                        "Kunne ikke hente adressebeskyttelsegradering for person. BehandlingId: $behandlingId",
                    )
                }

        require(adressebeskyttelseGradering != null) { "Fant ikke adressebeskyttelse for person. BehandlingId: $behandlingId" }

        val sakStatistikk = iverksettBehandlingMapper(
            sak = sak,
            behandling = iverksattBehandling,
            vedtak = vedtak,
            gjelderKode6 = adressebeskyttelseGradering.harStrengtFortroligAdresse(),
            versjon = gitHash,
        )
        val stønadStatistikk = stønadStatistikkMapper(sak, vedtak)
        val førsteMeldekort = vedtak.opprettFørsteMeldekortForEnSak()
        sessionFactory.withTransactionContext { tx ->
            førstegangsbehandlingRepo.lagre(iverksattBehandling, tx)
            rammevedtakRepo.lagre(vedtak, tx)
            statistikkSakRepo.lagre(sakStatistikk, tx)
            statistikkStønadRepo.lagre(stønadStatistikk, tx)
            meldekortRepo.lagre(førsteMeldekort, tx)
        }
        // journalføring og dokumentdistribusjon skjer i egen jobb
    }

    override suspend fun taBehandling(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Behandling {
        val behandling = hentBehandling(behandlingId, saksbehandler, correlationId)
        return behandling.taBehandling(saksbehandler).also {
            førstegangsbehandlingRepo.lagre(it)
        }
    }

    // er tenkt brukt fra datadeling og henter alle behandlinger som ikke er iverksatt for en ident
    override fun hentBehandlingerUnderBehandlingForIdent(
        ident: Fnr,
        fom: LocalDate,
        tom: LocalDate,
    ): List<Behandling> =
        førstegangsbehandlingRepo
            .hentAlleForIdent(ident)
            .filter { behandling -> !behandling.erIverksatt() }
            .filter { behandling ->
                behandling.vurderingsperiode.overlapperMed(
                    Periode(
                        fraOgMed = fom,
                        tilOgMed = tom,
                    ),
                )
            }

    private suspend fun sjekkTilgang(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ) {
        val fnr = personService.hentFnrForBehandlingId(behandlingId)
        tilgangsstyringService
            .harTilgangTilPerson(
                fnr = fnr,
                roller = saksbehandler.roller,
                correlationId = correlationId,
            )
            .onLeft { underliggendeFeil ->
                sikkerlogg.error(
                    underliggendeFeil.exception ?: IllegalArgumentException("Trigger en stacktrace for debugging"),
                ) { "Feil ved sjekk av tilgang til person. BehandlingId: $behandlingId. CorrelationId: $correlationId. body: ${underliggendeFeil.body}, status: ${underliggendeFeil.status}" }
                throw IkkeFunnetException("Feil ved sjekk av tilgang til person. BehandlingId: $behandlingId. CorrelationId: $correlationId. Feiltype: ${underliggendeFeil::class.simpleName} Se sikkerlogg for mer context")
            }
            .onRight { if (!it) throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til person") }
    }
}
