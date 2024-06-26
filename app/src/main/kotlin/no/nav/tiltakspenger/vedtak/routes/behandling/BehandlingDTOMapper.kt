package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal object BehandlingDTOMapper {
    fun List<Førstegangsbehandling>.mapBehandlinger(): List<BehandlingDTO> =
        this.map {
            BehandlingDTO(
                id = it.id.toString(),
                ident = it.søknad().personopplysninger.ident,
                saksbehandler = it.saksbehandler,
                beslutter = it.beslutter,
                status = finnStatus(it),
                typeBehandling = "Førstegangsbehandling",
                fom = it.vurderingsperiode.fraOgMed,
                tom = it.vurderingsperiode.tilOgMed,
                vilkårssett = it.vilkårssett.toDTO(it.vurderingsperiode.toDTO()),
            )
        }.sortedBy { it.id }
}
