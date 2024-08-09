package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.vedtak.routes.behandling.stønadsdager.StønadsdagerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.VilkårssettDTO
import java.time.LocalDate

internal data class BehandlingDTO(
    val id: String,
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val typeBehandling: String,
    val status: BehandlingsstatusDTO,
    val saksbehandler: String?,
    val beslutter: String?,
    val vilkårssett: VilkårssettDTO,
    val stønadsdager: StønadsdagerDTO,
)
