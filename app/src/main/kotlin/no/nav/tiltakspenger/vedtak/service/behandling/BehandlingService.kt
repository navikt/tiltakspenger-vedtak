package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler

interface BehandlingService {
    fun hentBehandling(behandlingId: BehandlingId): Søknadsbehandling?
    fun hentAlleBehandlinger(): List<Søknadsbehandling>

    fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning)
    fun sendTilBeslutter(behandlingId: BehandlingId, saksbehandler: String)
}
