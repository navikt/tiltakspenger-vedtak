package no.nav.tiltakspenger.vedtak.routes.behandling.mapper

import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus
import no.nav.tiltakspenger.vedtak.routes.behandling.dto.BehandlingDTO

object BehandlingDTOMapper {
    fun List<Førstegangsbehandling>.mapBehandlinger(): List<BehandlingDTO> =
        this.map {
            BehandlingDTO(
                id = it.id.toString(),
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
