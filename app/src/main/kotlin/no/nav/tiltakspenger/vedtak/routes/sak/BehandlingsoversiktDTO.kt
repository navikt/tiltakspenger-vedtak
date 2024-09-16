package no.nav.tiltakspenger.vedtak.routes.sak

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

data class BehandlingsoversiktDTO(
    val periode: PeriodeDTO?,
    val status: String,
    val kravtidspunkt: String,
    val underkjent: Boolean?,
    val typeBehandling: TypeBehandling,
    val fnr: String,
    val id: String,
    val saksbehandler: String?,
    val beslutter: String?,
) {
    enum class TypeBehandling {
        Førstegangsbehandling,
    }
}

fun List<Behandling>.toBehandlingOversiktDTO(): List<BehandlingsoversiktDTO> =
    this.map { it.toOversiktDTO() }

fun Behandling.toOversiktDTO() = BehandlingsoversiktDTO(
    periode = vurderingsperiode.toDTO(),
    status = status.toDTO().toString(),
    kravtidspunkt = vilkårssett.kravfristVilkår.avklartSaksopplysning.kravdato.toString(),
    underkjent = attesteringer.any { attestering -> attestering.isUnderkjent() },
    typeBehandling = BehandlingsoversiktDTO.TypeBehandling.Førstegangsbehandling,
    fnr = fnr.verdi,
    id = id.toString(),
    saksbehandler = saksbehandler,
    beslutter = beslutter,

)
