package no.nav.tiltakspenger.vedtak.service.behandling

import io.ktor.server.plugins.NotFoundException
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.attestering.Attestering
import no.nav.tiltakspenger.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.domene.behandling.Tiltak
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.vedtak.service.ports.BehandlingRepo
import no.nav.tiltakspenger.vedtak.service.ports.BrevPublisherGateway
import no.nav.tiltakspenger.vedtak.service.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.vedtak.service.ports.MultiRepo
import no.nav.tiltakspenger.vedtak.service.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.service.utbetaling.UtbetalingService
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class BehandlingServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val personopplysningRepo: PersonopplysningerRepo,
    private val utbetalingService: UtbetalingService,
    private val brevPublisherGateway: BrevPublisherGateway,
    private val meldekortGrunnlagGateway: MeldekortGrunnlagGateway,
    private val multiRepo: MultiRepo,
) : BehandlingService {

    override fun hentBehandlingOrNull(behandlingId: BehandlingId): Førstegangsbehandling? {
        return behandlingRepo.hent(behandlingId)
    }

    override fun hentBehandlingForJournalpostId(journalpostId: String): Førstegangsbehandling? {
        return behandlingRepo.hentForJournalpostId(journalpostId)
    }

    override fun hentAlleBehandlinger(saksbehandler: Saksbehandler): List<Førstegangsbehandling> {
        return behandlingRepo.hentAlle()
            .filter { behandling -> personopplysningRepo.hent(behandling.sakId).harTilgang(saksbehandler) }
    }

    override fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning) {
        val behandlingRespons = hentBehandling(behandlingId)
            .leggTilSaksopplysning(saksopplysning)
        if (behandlingRespons.erEndret) behandlingRepo.lagre(behandlingRespons.behandling)
    }

    override fun oppdaterTiltak(behandlingId: BehandlingId, tiltak: List<Tiltak>) {
        val behandling = hentBehandling(behandlingId)
        val oppdatertBehandling = behandling.oppdaterTiltak(
            tiltak.filter {
                Periode(it.deltakelseFom, it.deltakelseTom).overlapperMed(behandling.vurderingsperiode)
            },
        )
        behandlingRepo.lagre(oppdatertBehandling)
    }

    override fun sendTilBeslutter(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ) {
        check(utøvendeSaksbehandler.roller.contains(Rolle.SAKSBEHANDLER)) { "Saksbehandler må være saksbehandler" }
        val behandling = hentBehandling(behandlingId)
        if (behandling is BehandlingVilkårsvurdert) {
            behandlingRepo.lagre(behandling.tilBeslutting(utøvendeSaksbehandler))
        }
    }

    override fun sendTilbakeTilSaksbehandler(
        behandlingId: BehandlingId,
        utøvendeBeslutter: Saksbehandler,
        begrunnelse: String?,
    ) {
        val behandling = hentBehandling(behandlingId)

        checkNotNull(begrunnelse) { "Begrunnelse må oppgis når behandling sendes tilbake til saksbehandler" }
        val attestering = Attestering(
            behandlingId = behandlingId,
            svar = AttesteringStatus.SENDT_TILBAKE,
            begrunnelse = begrunnelse,
            beslutter = utøvendeBeslutter.navIdent,
        )

        when (behandling) {
            is BehandlingTilBeslutter -> {
                multiRepo.lagre(behandling.sendTilbake(utøvendeBeslutter), attestering)
            }

            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke sendes tilbake til saksbehandler. BehandlingId: $behandlingId")
        }
    }

    override suspend fun iverksett(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler) {
        val behandling = hentBehandling(behandlingId)

        val iverksattBehandling = when (behandling) {
            is BehandlingTilBeslutter -> behandling.iverksett(utøvendeBeslutter)
            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke iverksettes. BehandlingId: $behandlingId")
        }
        val attestering = Attestering(
            behandlingId = behandlingId,
            svar = AttesteringStatus.GODKJENT,
            begrunnelse = null,
            beslutter = utøvendeBeslutter.navIdent,
        )

        val vedtak = lagVedtakForBehandling(iverksattBehandling)
        multiRepo.lagreOgKjør(iverksattBehandling, attestering, vedtak) {
            // Hvis kallet til utbetalingService feiler, kastes det en exception slik at vi ikke lagrer vedtaket og
            // sender melding til brev og meldekortgrunnlag. Dette er med vilje.
            utbetalingService.sendBehandlingTilUtbetaling(vedtak)
        }

        meldekortGrunnlagGateway.sendMeldekortGrunnlag(vedtak)

        val personopplysninger =
            personopplysningRepo.hent(vedtak.sakId).søker()
        brevPublisherGateway.sendBrev(vedtak, personopplysninger)
    }

    private fun lagVedtakForBehandling(behandling: BehandlingIverksatt): Vedtak {
        return Vedtak(
            id = VedtakId.random(),
            sakId = behandling.sakId,
            behandling = behandling,
            vedtaksdato = LocalDateTime.now(),
            vedtaksType = if (behandling.status == BehandlingStatus.Innvilget) VedtaksType.INNVILGELSE else VedtaksType.AVSLAG,
            utfallsperioder = behandling.utfallsperioder,
            periode = behandling.vurderingsperiode,
            saksopplysninger = behandling.saksopplysninger(),
            vurderinger = behandling.vilkårsvurderinger,
            saksbehandler = behandling.saksbehandler,
            beslutter = behandling.beslutter,
        )
    }

    // TODO: Burde det vært to ulike funksjoner avhengig av om det er saksbehandler eller beslutter det gjelder?
    override fun taBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler) {
        val behandling = hentBehandling(behandlingId)
        behandlingRepo.lagre(behandling.startBehandling(utøvendeSaksbehandler))
    }

    override fun frataBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler) {
        val behandling = hentBehandling(behandlingId)
        behandlingRepo.lagre(behandling.avbrytBehandling(utøvendeSaksbehandler))
    }

    override fun hentBehandlingForIdent(
        ident: String,
        utøvendeSaksbehandler: Saksbehandler,
    ): List<Førstegangsbehandling> {
        return behandlingRepo.hentAlleForIdent(ident)
            .filter { behandling -> personopplysningRepo.hent(behandling.sakId).harTilgang(utøvendeSaksbehandler) }
    }

    private fun hentBehandling(behandlingId: BehandlingId): Behandling =
        hentBehandlingOrNull(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")
}
