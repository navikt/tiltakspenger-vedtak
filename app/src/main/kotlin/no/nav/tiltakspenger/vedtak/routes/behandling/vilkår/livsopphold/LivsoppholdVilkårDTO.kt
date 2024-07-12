package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class LivsoppholdVilkårDTO(
    val avklartSaksopplysning: LivsoppholdSaksopplysningDTO,
    val vurderingsPeriode: PeriodeDTO,
)

internal fun LivsoppholdVilkår.toDTO(): LivsoppholdVilkårDTO {
    val avklartSaksopplysning = if (saksbehandlerSaksopplysning == null) søknadssaksopplysning else saksbehandlerSaksopplysning
    return LivsoppholdVilkårDTO(
        avklartSaksopplysning = avklartSaksopplysning!!.toDTO(),
        vurderingsPeriode = vurderingsPeriode.toDTO(),
    )
}
