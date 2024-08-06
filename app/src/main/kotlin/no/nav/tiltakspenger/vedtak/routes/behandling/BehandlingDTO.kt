package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.VilkårssettDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class BehandlingDTO(
    val id: String,
    val saksbehandler: String?,
    val beslutter: String?,
    val vurderingsperiode: PeriodeDTO,
    val status: BehandlingStatus,
    val vilkårssett: VilkårssettDTO,
    // val attesteringer: List<AttesteringDTO>,
)

internal fun Behandling.toDTO() = BehandlingDTO(
    id = this.id.toString(),
    saksbehandler = this.saksbehandler,
    beslutter = this.beslutter,
    status = this.status,
    vurderingsperiode = this.vurderingsperiode.toDTO(),
    // attesteringer = this.attesteringer.toDTO,
    vilkårssett = this.vilkårssett.toDTO(),
)
