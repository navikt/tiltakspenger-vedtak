package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.domene.behandling.Tiltak
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler

interface BehandlingService {
    fun hentBehandling(behandlingId: BehandlingId): Førstegangsbehandling?
    fun hentBehandlingForJournalpostId(journalpostId: String): Førstegangsbehandling?
    fun hentAlleBehandlinger(): List<Førstegangsbehandling>
    fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning)
    fun oppdaterTiltak(behandlingId: BehandlingId, tiltak: List<Tiltak>)
    fun sendTilBeslutter(behandlingId: BehandlingId, saksbehandler: String)
    fun sendTilbakeTilSaksbehandler(
        behandlingId: BehandlingId,
        beslutter: String,
        begrunnelse: String?,
        isAdmin: Boolean,
    )

    suspend fun iverksett(behandlingId: BehandlingId, saksbehandler: String)
    fun startBehandling(behandlingId: BehandlingId, saksbehandler: String)
    fun avbrytBehandling(behandlingId: BehandlingId, saksbehandler: Saksbehandler)
    fun hentBehandlingForIdent(ident: String): List<Førstegangsbehandling>
}
