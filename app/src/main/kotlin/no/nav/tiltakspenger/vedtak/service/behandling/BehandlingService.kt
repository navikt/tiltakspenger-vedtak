package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId

interface BehandlingService {
    fun hentBehandling(behandlingId: BehandlingId): Søknadsbehandling?
    fun hentBehandlingForJournalpostId(journalpostId: String): Søknadsbehandling?
    fun hentAlleBehandlinger(): List<Søknadsbehandling>
    fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning)
    fun sendTilBeslutter(behandlingId: BehandlingId, saksbehandler: String)
    fun sendTilbakeTilSaksbehandler(behandlingId: BehandlingId)
}
