package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class LivsoppholdVilkårDTO(
    val avklartSaksopplysning: LivsoppholdSaksopplysningDTO?,
    val vurderingsperiode: PeriodeDTO,
    val vilkårLovreferanse: LovreferanseDTO,
    val utfallperiode: PeriodeDTO,
    val samletUtfall: SamletUtfallDTO,
)

internal fun LivsoppholdVilkår.toDTO(): LivsoppholdVilkårDTO {
    val samletUtfall = when (avklartSaksopplysning?.harLivsoppholdYtelser) {
        true -> SamletUtfallDTO.IKKE_OPPFYLT
        false -> SamletUtfallDTO.OPPFYLT
        null -> SamletUtfallDTO.UAVKLART
    }

    return LivsoppholdVilkårDTO(
        avklartSaksopplysning = avklartSaksopplysning?.toDTO(vurderingsperiode = vurderingsperiode.toDTO()),
        vurderingsperiode = vurderingsperiode.toDTO(),
        vilkårLovreferanse = lovreferanse.toDTO(),
        utfallperiode = this.utfall().totalePeriode.toDTO(),
        samletUtfall = samletUtfall,
    )
}
