package no.nav.tiltakspenger.vedtak.service.behandling

import io.ktor.server.plugins.NotFoundException
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.attestering.Attestering
import no.nav.tiltakspenger.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.domene.behandling.RevurderingOpprettet
import no.nav.tiltakspenger.domene.behandling.Revurderingsbehandling
import no.nav.tiltakspenger.domene.behandling.Tiltak
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.service.personopplysning.PersonopplysningService
import no.nav.tiltakspenger.vedtak.service.vedtak.VedtakService

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class BehandlingServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val vedtakService: VedtakService,
    private val attesteringRepo: AttesteringRepo,
    private val personopplysningService: PersonopplysningService,
) : BehandlingService {

    override fun hentBehandlingOrNull(behandlingId: BehandlingId): Førstegangsbehandling? {
        return behandlingRepo.hent(behandlingId)
    }

    override fun hentBehandlingForJournalpostId(journalpostId: String): Førstegangsbehandling? {
        return behandlingRepo.hentForJournalpostId(journalpostId)
    }

    override fun hentAlleBehandlinger(saksbehandler: Saksbehandler): List<Førstegangsbehandling> {
        return behandlingRepo.hentAlle()
            .filter { behandling -> personopplysningService.hent(behandling.sakId).harTilgang(saksbehandler) }
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
                sessionOf(DataSource.hikariDataSource).use {
                    it.transaction { txSession ->
                        behandlingRepo.lagre(behandling.sendTilbake(utøvendeBeslutter), txSession)
                        attesteringRepo.lagre(attestering, txSession)
                    }
                }
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
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                behandlingRepo.lagre(iverksattBehandling, txSession)
                attesteringRepo.lagre(attestering, txSession)
                vedtakService.lagVedtakForBehandling(iverksattBehandling, txSession)
            }
        }
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
            .filter { behandling -> personopplysningService.hent(behandling.sakId).harTilgang(utøvendeSaksbehandler) }
    }

    private fun hentBehandling(behandlingId: BehandlingId): Behandling =
        hentBehandlingOrNull(behandlingId)
            ?: throw NotFoundException("Fant ikke behandlingen med behandlingId: $behandlingId")

    override fun opprettRevurdering(behandlingId: BehandlingId): Revurderingsbehandling {
        val iverksattBehandling = behandlingRepo.hent(behandlingId) as BehandlingIverksatt
        val revurderingBehandling = RevurderingOpprettet.opprettRevurderingsbehandling(
            behandlingIverksatt = iverksattBehandling,
        )
        return revurderingBehandling
    }
}
