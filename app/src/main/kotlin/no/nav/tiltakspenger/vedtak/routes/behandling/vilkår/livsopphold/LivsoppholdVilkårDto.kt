package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.AAPDelVilkaar
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.AlderspensjonDelVilkaar
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkaar
import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class LivsoppholdVilkårDto(
    val aapDelVilkår: AAPDelVilkårDTO,
    val alderspensjonDelVilkår: AlderspensjonDelVilkårDTO,
    val vilkårLovreferanse: LovreferanseDTO,
    val vurderingsperiode: PeriodeDTO,
    val samletUtfall: SamletUtfallDTO,
) {
    internal data class AAPDelVilkårDTO(
        val saksbehandlerSaksopplysning: LivsoppholdSaksopplysningDto?,
        val vilkårLovreferanse: LovreferanseDTO,
        val vurderingsperiode: PeriodeDTO,
        val samletUtfall: SamletUtfallDTO,
    )

    internal data class AlderspensjonDelVilkårDTO(
        val søknadSaksopplysning: LivsoppholdSaksopplysningDto,
        val avklartSaksopplysning: LivsoppholdSaksopplysningDto,
        val vilkårLovreferanse: LovreferanseDTO,
        val vurderingsperiode: PeriodeDTO,
        val samletUtfall: SamletUtfallDTO,
    )
}

internal fun LivsoppholdVilkaar.toDTO(): LivsoppholdVilkårDto {
    return LivsoppholdVilkårDto(
        aapDelVilkår = aapDelVilkår.toDTO(),
        alderspensjonDelVilkår = alderspensjonDelVilkår.toDTO(),
        vilkårLovreferanse = lovreferanse.toDTO(),
        vurderingsperiode = vurderingsperiode.toDTO(),
        samletUtfall = this.samletUtfall.toDTO(),
    )
}

internal fun AAPDelVilkaar.toDTO(): LivsoppholdVilkårDto.AAPDelVilkårDTO {
    return TODO()
}

internal fun AlderspensjonDelVilkaar.toDTO(): LivsoppholdVilkårDto.AlderspensjonDelVilkårDTO {
    return TODO()
}
