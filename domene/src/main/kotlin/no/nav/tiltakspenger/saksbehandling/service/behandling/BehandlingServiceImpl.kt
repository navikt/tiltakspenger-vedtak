package no.nav.tiltakspenger.saksbehandling.service.behandling

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attesteringsstatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.opprettVedtak
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortgrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.iverksettBehandlingMapper
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.stønadStatistikkMapper
import java.time.LocalDate

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class BehandlingServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val vedtakRepo: RammevedtakRepo,
    private val personopplysningRepo: PersonopplysningerRepo,
    private val brevPublisherGateway: BrevPublisherGateway,
    private val meldekortGrunnlagGateway: MeldekortgrunnlagGateway,
    private val sakRepo: SakRepo,
    private val sessionFactory: SessionFactory,
    private val saksoversiktRepo: SaksoversiktRepo,
    private val statistikkSakRepo: StatistikkSakRepo,
    private val statistikkStønadRepo: StatistikkStønadRepo,
) : BehandlingService {
    override fun hentBehandling(
        behandlingId: BehandlingId,
        sessionContext: SessionContext?,
    ): Behandling = behandlingRepo.hent(behandlingId, sessionContext)

    override fun hentBehandling(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        sessionContext: SessionContext?,
    ): Behandling {
        val behandling = hentBehandling(behandlingId, sessionContext)
        val sakPersonopplysninger = personopplysningRepo.hent(behandling.sakId)
        if (!sakPersonopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler har ikke tilgang til behandling")
        }
        return behandling
    }

    override fun hentBehandlingForSøknadId(søknadId: SøknadId): Førstegangsbehandling? {
        // TODO pre-mvp tilgang jah: Legg på sjekk på kode 6/7/skjermet.
        return behandlingRepo.hentForSøknadId(søknadId)
    }

    override fun hentSaksoversikt(saksbehandler: Saksbehandler): Saksoversikt {
        require(saksbehandler.isSaksbehandler())
        // TODO pre-mvp tilgang jah: Legg på sjekk på kode 6/7/skjermet. Filtrerer vi bare bort de som er skjermet?
        return saksoversiktRepo.hentAlle()
    }

    override fun sendTilBeslutter(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ) {
        val behandling = hentBehandling(behandlingId, utøvendeSaksbehandler).tilBeslutning(utøvendeSaksbehandler)
        behandlingRepo.lagre(behandling)
    }

    override fun sendTilbakeTilSaksbehandler(
        behandlingId: BehandlingId,
        utøvendeBeslutter: Saksbehandler,
        begrunnelse: String,
    ) {
        val attestering =
            Attestering(
                status = Attesteringsstatus.SENDT_TILBAKE,
                begrunnelse = begrunnelse,
                beslutter = utøvendeBeslutter.navIdent,
            )
        val behandling = hentBehandling(behandlingId, utøvendeBeslutter).sendTilbake(utøvendeBeslutter, attestering)
        sessionFactory.withTransactionContext { tx ->
            behandlingRepo.lagre(behandling, tx)
        }
    }

    override suspend fun iverksett(
        behandlingId: BehandlingId,
        utøvendeBeslutter: Saksbehandler,
    ) {
        val behandling = hentBehandling(behandlingId, utøvendeBeslutter) as Førstegangsbehandling
        val sak =
            sakRepo.hentSakDetaljer(behandling.sakId)
                ?: throw IllegalStateException("iverksett finner ikke sak ${behandling.sakId}")
        val attestering =
            Attestering(
                status = Attesteringsstatus.GODKJENT,
                begrunnelse = "",
                beslutter = utøvendeBeslutter.navIdent,
            )
        val iverksattBehandling = behandling.iverksett(utøvendeBeslutter, attestering)

        val vedtak = iverksattBehandling.opprettVedtak()
        val sakStatistikk = iverksettBehandlingMapper(sak, iverksattBehandling, vedtak)
        val stønadStatistikk = stønadStatistikkMapper(sak, vedtak)
        sessionFactory.withTransactionContext { tx ->
            behandlingRepo.lagre(iverksattBehandling, tx)
            vedtakRepo.lagreVedtak(vedtak, tx)
            statistikkSakRepo.lagre(sakStatistikk, tx)
            statistikkStønadRepo.lagre(stønadStatistikk, tx)
        }
        // Meldekortgrunnlag sendes etter vedtaket er lagret fra en separat jobb.

        val personopplysninger = personopplysningRepo.hent(vedtak.sakId).søker()
        brevPublisherGateway.sendBrev(sak.saksnummer, vedtak, personopplysninger)
    }

    override fun taBehandling(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ): Behandling {
        val behandling = hentBehandling(behandlingId, utøvendeSaksbehandler)
        return behandling.taBehandling(utøvendeSaksbehandler).also {
            behandlingRepo.lagre(it)
        }
    }

    override fun frataBehandling(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ) {
        val behandling = hentBehandling(behandlingId, utøvendeSaksbehandler)
        behandlingRepo.lagre(behandling.taSaksbehandlerAvBehandlingen(utøvendeSaksbehandler))
    }

    // er tenkt brukt fra datadeling og henter alle behandlinger som ikke er iverksatt for en ident
    override fun hentBehandlingerUnderBehandlingForIdent(
        ident: Fnr,
        fom: LocalDate,
        tom: LocalDate,
    ): List<Behandling> =
        behandlingRepo
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
}
