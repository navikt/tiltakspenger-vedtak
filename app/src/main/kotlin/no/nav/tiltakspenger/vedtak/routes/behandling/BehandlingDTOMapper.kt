package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus

object BehandlingDTOMapper {
    fun List<Førstegangsbehandling>.mapBehandlinger(): List<BehandlingDTO> =
        this.map {
            BehandlingDTO(
                id = it.id.toString(),
                ident = it.søknad().personopplysninger.ident,
                saksbehandler = it.saksbehandler,
                beslutter = when (it.tilstand) {
                    BehandlingTilstand.IVERKSATT -> it.beslutter
                    BehandlingTilstand.TIL_BESLUTTER -> it.beslutter
                    else -> null
                },
                status = finnStatus(it),
                typeBehandling = "Førstegangsbehandling",
                fom = it.vurderingsperiode.fra,
                tom = it.vurderingsperiode.til,
            )
        }.sortedBy { it.id }
}
