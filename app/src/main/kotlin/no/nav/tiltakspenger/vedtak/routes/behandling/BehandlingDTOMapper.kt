package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.vedtak.routes.behandling.stønadsdager.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO

internal object BehandlingDTOMapper {
    fun List<Førstegangsbehandling>.mapBehandlinger(): List<BehandlingDTO> =
        this
            .map {
                BehandlingDTO(
                    id = it.id.toString(),
                    ident = it.søknad.personopplysninger.fnr.verdi,
                    saksbehandler = it.saksbehandler,
                    beslutter = it.beslutter,
                    status = it.status.toDTO(),
                    typeBehandling = "Førstegangsbehandling",
                    fom = it.vurderingsperiode.fraOgMed,
                    tom = it.vurderingsperiode.tilOgMed,
                    vilkårssett = it.vilkårssett.toDTO(),
                    stønadsdager = it.stønadsdager.toDTO(),
                )
            }.sortedBy { it.id }
}
