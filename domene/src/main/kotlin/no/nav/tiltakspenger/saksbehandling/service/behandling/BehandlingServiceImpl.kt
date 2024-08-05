package no.nav.tiltakspenger.saksbehandling.service.behandling

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.opprettVedtak
import no.nav.tiltakspenger.saksbehandling.ports.AttesteringRepo
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class BehandlingServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val vedtakRepo: VedtakRepo,
    private val personopplysningRepo: PersonopplysningerRepo,
    private val brevPublisherGateway: BrevPublisherGateway,
    private val meldekortGrunnlagGateway: MeldekortGrunnlagGateway,
    private val sakRepo: SakRepo,
    private val attesteringRepo: AttesteringRepo,
    private val sessionFactory: SessionFactory,
    private val saksoversiktRepo: SaksoversiktRepo,
) : BehandlingService {

    override fun hentBehandling(behandlingId: BehandlingId, sessionContext: SessionContext?): Behandling {
        return behandlingRepo.hent(behandlingId, sessionContext)
    }

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
        // TODO tilgang jah: Legg på sjekk på kode 6/7/skjermet.
        return behandlingRepo.hentForSøknadId(søknadId)
    }

    override fun hentSaksoversikt(saksbehandler: Saksbehandler): Saksoversikt {
        require(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin())
        // TODO tilgang jah: Legg på sjekk på kode 6/7/skjermet. Filtrerer vi bare bort de som er skjermet?
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
        val behandling = hentBehandling(behandlingId, utøvendeBeslutter).sendTilbake(utøvendeBeslutter)

        val attestering = Attestering(
            behandlingId = behandlingId,
            svar = AttesteringStatus.SENDT_TILBAKE,
            begrunnelse = begrunnelse,
            beslutter = utøvendeBeslutter.navIdent,
        )
        sessionFactory.withTransactionContext { tx ->
            behandlingRepo.lagre(behandling, tx)
            attesteringRepo.lagre(attestering, tx)
        }
    }

    override suspend fun iverksett(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler) {
        val behandling = hentBehandling(behandlingId, utøvendeBeslutter) as Førstegangsbehandling
        val sak = sakRepo.hentSakDetaljer(behandling.sakId)
            ?: throw IllegalStateException("iverksett finner ikke sak ${behandling.sakId}")

        val iverksattBehandling = behandling.iverksett(utøvendeBeslutter)
        val attestering = Attestering(
            behandlingId = behandlingId,
            svar = AttesteringStatus.GODKJENT,
            begrunnelse = null,
            beslutter = utøvendeBeslutter.navIdent,
        )

        val vedtak = iverksattBehandling.opprettVedtak()
        sessionFactory.withTransactionContext { tx ->
            behandlingRepo.lagre(iverksattBehandling, tx)
            attesteringRepo.lagre(attestering, tx)
            vedtakRepo.lagreVedtak(vedtak, tx)
        }

        meldekortGrunnlagGateway.sendMeldekortGrunnlag(sak, vedtak)

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

    override fun frataBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler) {
        val behandling = hentBehandling(behandlingId, utøvendeSaksbehandler)
        behandlingRepo.lagre(behandling.taSaksbehandlerAvBehandlingen(utøvendeSaksbehandler))
    }
}
