package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

object StatusMapper {
    fun finnStatus(behandling: Førstegangsbehandling): String =
        when (behandling.tilstand) {
            BehandlingTilstand.IVERKSATT -> if (behandling.status == BehandlingStatus.Avslag) "Iverksatt Avslag" else "Iverksatt Innvilget"
            BehandlingTilstand.TIL_BESLUTTER -> if (behandling.beslutter == null) "Klar til beslutning" else "Under beslutning"
            BehandlingTilstand.VILKÅRSVURDERT -> if (behandling.saksbehandler == null) "Klar til behandling" else "Under behandling"
            BehandlingTilstand.OPPRETTET -> "Klar til behandling"
        }
}
