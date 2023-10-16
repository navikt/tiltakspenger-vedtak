package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId

interface BehandlingService {
    fun hentBehandling(behandlingId: BehandlingId): Søknadsbehandling?
    fun hentAlleBehandlinger(): List<Søknadsbehandling>

    fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning)
    fun endreTilstand(behandlingId: BehandlingId, tilstand: String)
}
