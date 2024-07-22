package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold.InstitusjonsoppholdVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.IntroVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KVPVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.LivsoppholdVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class VilkårssettDTO(
    val kvpVilkår: KVPVilkårDTO,
    val introVilkår: IntroVilkårDTO,
    val institusjonsoppholdVilkår: InstitusjonsoppholdVilkårDTO,
    val livsoppholdVilkår: LivsoppholdVilkårDTO,
)

internal fun Vilkårssett.toDTO(vurderingsperiode: PeriodeDTO): VilkårssettDTO {
    return VilkårssettDTO(
        kvpVilkår = kvpVilkår.toDTO(vurderingsperiode),
        introVilkår = introVilkår.toDTO(vurderingsperiode),
        institusjonsoppholdVilkår = institusjonsoppholdVilkår.toDTO(vurderingsperiode),
        livsoppholdVilkår = livsoppholdVilkår.toDTO(),
    )
}
