package no.nav.tiltakspenger.vedtak.service.behandling

import io.ktor.server.plugins.NotFoundException
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.attestering.Attestering
import no.nav.tiltakspenger.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.behandling.Tiltak
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.service.vedtak.VedtakService

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class BehandlingServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val vedtakService: VedtakService,
    private val attesteringRepo: AttesteringRepo,
) : BehandlingService {

    override fun hentBehandling(behandlingId: BehandlingId): Søknadsbehandling? {
        return behandlingRepo.hent(behandlingId)
    }

    override fun hentBehandlingForJournalpostId(journalpostId: String): Søknadsbehandling? {
        return behandlingRepo.hentForJournalpostId(journalpostId)
    }

    override fun hentAlleBehandlinger(): List<Søknadsbehandling> {
        return behandlingRepo.hentAlle()
    }

    override fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning) {
        val behandlingRespons = hentBehandling(behandlingId)?.leggTilSaksopplysning(saksopplysning)
            ?: throw IllegalStateException("Kunne ikke legge til saksopplysning da vi ikke fant behandling $behandlingId")
        if (behandlingRespons.erEndret) behandlingRepo.lagre(behandlingRespons.behandling)
    }

    override fun oppdaterTiltak(behandlingId: BehandlingId, tiltak: List<Tiltak>) {
        val behandling = hentBehandling(behandlingId)?.oppdaterTiltak(tiltak)
            ?: throw IllegalStateException("Kunnde ikke oppdatere tiltak da vi ikke fant behandling $behandlingId")
        behandlingRepo.lagre(behandling)
    }

    override fun sendTilBeslutter(behandlingId: BehandlingId, saksbehandler: String) {
        val behandling = hentBehandling(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")
        check(saksbehandler == behandling.saksbehandler) { "Det er ikke lov å sende en annen sin behandling til beslutter" }
        when (behandling) {
            is BehandlingVilkårsvurdert.Avslag -> behandlingRepo.lagre(behandling.tilBeslutting())
            is BehandlingVilkårsvurdert.Innvilget -> behandlingRepo.lagre(behandling.tilBeslutting())
            else -> throw IllegalStateException("Behandlingen har feil status og kan ikke sendes til beslutting. BehandlingId: $behandlingId")
        }
    }

    override fun sendTilbakeTilSaksbehandler(behandlingId: BehandlingId, beslutter: String, begrunnelse: String?) {
        val behandling = hentBehandling(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")

        checkNotNull(begrunnelse) { "Begrunnelse må oppgis når behandling sendes tilbake til saksbehandler" }
        val attestering = Attestering(
            behandlingId = behandlingId,
            svar = AttesteringStatus.SENDT_TILBAKE,
            begrunnelse = begrunnelse,
            beslutter = beslutter,
        )

        when (behandling) {
            is BehandlingTilBeslutter -> {
                check(behandling.beslutter == beslutter) { "Det er ikke lov å sende en annen sin behandling tilbake til saksbehandler" }
                behandlingRepo.lagre(behandling.sendTilbake())
                attesteringRepo.lagre(attestering)
            }
            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke sendes tilbake til saksbehandler. BehandlingId: $behandlingId")
        }
    }

    override fun iverksett(behandlingId: BehandlingId, saksbehandler: String) {
        val behandling = hentBehandling(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")

        if (behandling is BehandlingTilBeslutter) {
            check(behandling.saksbehandler != null) { "Kan ikke iverksette en behandling uten saksbehandler" }
            check(behandling.beslutter == saksbehandler) { "Kan ikke iverksette en behandling man ikke er beslutter på" }
        }

        val iverksattBehandling = when (behandling) {
            is BehandlingTilBeslutter -> behandling.iverksett()
            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke iverksettes. BehandlingId: $behandlingId")
        }
        val attestering = Attestering(
            behandlingId = behandlingId,
            svar = AttesteringStatus.GODKJENT,
            begrunnelse = null,
            beslutter = saksbehandler,

        )
        behandlingRepo.lagre(iverksattBehandling)
        attesteringRepo.lagre(attestering)
        vedtakService.lagVedtakForBehandling(iverksattBehandling)
    }

    override fun startBehandling(behandlingId: BehandlingId, saksbehandler: String) {
        val behandling = hentBehandling(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")

        if (behandling.erÅpen()) {
            check(behandling.saksbehandler == null) { "Denne behandlingen er allerede tatt" }
        }

        if (behandling is BehandlingTilBeslutter) {
            check(behandling.saksbehandler != null) { "Kan ikke starte å beslutte en behandling uten saksbehandler" }
            check(behandling.beslutter == null) { "Denne behandlingen har allerede en beslutter" }
        }

        behandlingRepo.lagre(behandling.startBehandling(saksbehandler))
    }

    override fun avbrytBehandling(behandlingId: BehandlingId, saksbehandler: String) {
        val behandling = hentBehandling(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")

        check(behandling.saksbehandler == saksbehandler) { "Kan ikke avbryte en behandling som ikke er din" }
        behandlingRepo.lagre(behandling.avbrytBehandling())
    }
}
