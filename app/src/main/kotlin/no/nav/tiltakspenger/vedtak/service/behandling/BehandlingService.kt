package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.behandling.Tiltak
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler

interface BehandlingService {
    fun hentBehandling(behandlingId: BehandlingId): Søknadsbehandling?
    fun hentBehandlingForJournalpostId(journalpostId: String): Søknadsbehandling?
    fun hentAlleBehandlinger(): List<Søknadsbehandling>
    fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning)
    fun oppdaterTiltak(behandlingId: BehandlingId, tiltak: List<Tiltak>)
    fun sendTilBeslutter(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun sendTilbakeTilSaksbehandler(
        behandlingId: BehandlingId,
        utøvendeBeslutter: Saksbehandler,
        begrunnelse: String?,
    )

    suspend fun iverksett(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler)
    fun startBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun avbrytBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun hentBehandlingForIdent(ident: String, utøvendeSaksbehandler: Saksbehandler): List<Søknadsbehandling>
}
