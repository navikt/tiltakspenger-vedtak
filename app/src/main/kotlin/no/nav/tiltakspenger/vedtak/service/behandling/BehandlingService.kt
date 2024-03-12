package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.behandling.Tiltak
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler

interface BehandlingService {
    fun hentBehandlingOrNull(behandlingId: BehandlingId): Søknadsbehandling?
    fun hentBehandlingForJournalpostId(journalpostId: String): Søknadsbehandling?
    fun hentAlleBehandlinger(saksbehandler: Saksbehandler): List<Søknadsbehandling>
    fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning)
    fun oppdaterTiltak(behandlingId: BehandlingId, tiltak: List<Tiltak>)
    fun sendTilBeslutter(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun sendTilbakeTilSaksbehandler(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler, begrunnelse: String?)
    suspend fun iverksett(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler)
    fun taBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun frataBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun hentBehandlingForIdent(ident: String, utøvendeSaksbehandler: Saksbehandler): List<Søknadsbehandling>
}
