package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling

object StatusMapper {
    fun finnStatus(behandling: Førstegangsbehandling): String =
        when (behandling) {
            is BehandlingIverksatt -> if (behandling.status == BehandlingStatus.Avslag) "Iverksatt Avslag" else "Iverksatt Innvilget"
            is BehandlingTilBeslutter -> if (behandling.beslutter == null) "Klar til beslutning" else "Under beslutning"
            is BehandlingVilkårsvurdert -> if (behandling.saksbehandler == null) "Klar til behandling" else "Under behandling"
            is BehandlingOpprettet -> "Klar til behandling"
        }
}
