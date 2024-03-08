package no.nav.tiltakspenger.vedtak.service.behandling

import io.ktor.server.plugins.NotFoundException
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.attestering.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.saksbehandling.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.behandling.Søknadsbehandling
import no.nav.tiltakspenger.saksbehandling.behandling.Tiltak
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.vedtak.db.DataSource
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
        val behandling = hentBehandling(behandlingId)
            ?: throw IllegalStateException("Kunne ikke oppdatere tiltak da vi ikke fant behandling $behandlingId")
        val oppdatertBehandling = behandling.oppdaterTiltak(
            tiltak.filter {
                Periode(it.deltakelseFom, it.deltakelseTom).overlapperMed(behandling.vurderingsperiode)
            },
        )
        behandlingRepo.lagre(oppdatertBehandling)
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

    override fun sendTilbakeTilSaksbehandler(
        behandlingId: BehandlingId,
        beslutter: String,
        begrunnelse: String?,
        isAdmin: Boolean,
    ) {
        val behandling = hentBehandling(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")

        checkNotNull(begrunnelse) { "Begrunnelse må oppgis når behandling sendes tilbake til saksbehandler" }
        val attestering = no.nav.tiltakspenger.saksbehandling.attestering.Attestering(
            behandlingId = behandlingId,
            svar = no.nav.tiltakspenger.saksbehandling.attestering.AttesteringStatus.SENDT_TILBAKE,
            begrunnelse = begrunnelse,
            beslutter = beslutter,
        )

        when (behandling) {
            is BehandlingTilBeslutter -> {
                check(behandling.beslutter == beslutter || isAdmin) { "Det er ikke lov å sende en annen sin behandling tilbake til saksbehandler" }
                sessionOf(DataSource.hikariDataSource).use {
                    it.transaction { txSession ->
                        behandlingRepo.lagre(behandling.sendTilbake(), txSession)
                        attesteringRepo.lagre(attestering, txSession)
                    }
                }
            }

            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke sendes tilbake til saksbehandler. BehandlingId: $behandlingId")
        }
    }

    override suspend fun iverksett(behandlingId: BehandlingId, saksbehandler: String) {
        val behandling = hentBehandling(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")

        if (behandling is BehandlingTilBeslutter) {
            check(behandling.saksbehandler != null) { "Kan ikke iverksette en behandling uten saksbehandler" }
            check(behandling.beslutter == saksbehandler) { "Kan ikke iverksette en behandling man ikke er beslutter på" }
        }

        val iverksattBehandling = when (behandling) {
            is BehandlingTilBeslutter.Innvilget -> behandling.iverksett()
            is BehandlingTilBeslutter.Avslag -> throw IllegalStateException("Iverksett av Avslag fungerer, men skal ikke tillates i mvp 1 $behandling")
            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke iverksettes. BehandlingId: $behandlingId")
        }
        val attestering = no.nav.tiltakspenger.saksbehandling.attestering.Attestering(
            behandlingId = behandlingId,
            svar = no.nav.tiltakspenger.saksbehandling.attestering.AttesteringStatus.GODKJENT,
            begrunnelse = null,
            beslutter = saksbehandler,

        )

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                behandlingRepo.lagre(iverksattBehandling, txSession)
                attesteringRepo.lagre(attestering, txSession)
                vedtakService.lagVedtakForBehandling(iverksattBehandling, txSession)
            }
        }
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

    override fun avbrytBehandling(behandlingId: BehandlingId, saksbehandler: String, isAdmin: Boolean) {
        val behandling = hentBehandling(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")

        check(behandling.saksbehandler == saksbehandler || isAdmin) { "Kan ikke avbryte en behandling som ikke er din" }
        behandlingRepo.lagre(behandling.avbrytBehandling())
    }

    override fun hentBehandlingForIdent(ident: String): List<Søknadsbehandling> {
        return behandlingRepo.hentAlleForIdent(ident)
    }
}
