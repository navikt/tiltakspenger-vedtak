package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.behandling.Søknadsbehandling
import no.nav.tiltakspenger.saksbehandling.behandling.Tiltak
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Saksopplysning

interface BehandlingService {
    fun hentBehandling(behandlingId: BehandlingId): Søknadsbehandling?
    fun hentBehandlingForJournalpostId(journalpostId: String): Søknadsbehandling?
    fun hentAlleBehandlinger(): List<Søknadsbehandling>
    fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning)
    fun oppdaterTiltak(behandlingId: BehandlingId, tiltak: List<Tiltak>)
    fun sendTilBeslutter(behandlingId: BehandlingId, saksbehandler: String)
    fun sendTilbakeTilSaksbehandler(behandlingId: BehandlingId, beslutter: String, begrunnelse: String?, isAdmin: Boolean)
    suspend fun iverksett(behandlingId: BehandlingId, saksbehandler: String)
    fun startBehandling(behandlingId: BehandlingId, saksbehandler: String)
    fun avbrytBehandling(behandlingId: BehandlingId, saksbehandler: String, isAdmin: Boolean)
    fun hentBehandlingForIdent(ident: String): List<Søknadsbehandling>
}
