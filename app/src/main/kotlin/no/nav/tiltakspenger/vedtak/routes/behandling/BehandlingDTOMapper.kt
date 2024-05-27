package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus

object BehandlingDTOMapper {
    fun List<Førstegangsbehandling>.mapBehandlinger(): List<BehandlingDTO> =
        this.map {
            BehandlingDTO(
                id = it.id.toString(),
                // TODO: Fy fy å hente personopplysninger fra søknaden her? Det bør håndteres som en saksopplysning?
                ident = it.søknad().personopplysninger.ident,
                saksbehandler = it.saksbehandler,
                beslutter = when (it) {
                    is BehandlingIverksatt -> it.beslutter
                    is BehandlingTilBeslutter -> it.beslutter
                    else -> null
                },
                status = finnStatus(it),
                typeBehandling = "Førstegangsbehandling",
                fom = it.vurderingsperiode.fra,
                tom = it.vurderingsperiode.til,
            )
        }.sortedBy { it.id }
}
