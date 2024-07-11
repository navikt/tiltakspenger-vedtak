package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class LivsoppholdVilkårDTO (
    val søknadssaksopplysning: LivsoppholdSaksopplysningDTO,
    val saksbehandlerSaksopplysning: LivsoppholdSaksopplysningDTO?,
    val vurderingsPeriode: PeriodeDTO,
)

internal fun LivsoppholdVilkår.toDTO(): LivsoppholdVilkårDTO {
    return LivsoppholdVilkårDTO(
        harEnEllerFlereYtelserFraSøknaden = søknadssaksopplysning,
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDTO(),
        vurderingsPeriode = vurderingsPeriode.toDTO()
    )
}
