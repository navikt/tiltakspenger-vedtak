package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.alder.AlderVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.alder.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold.InstitusjonsoppholdVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.IntroVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.KravfristVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KVPVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.LivsoppholdVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.toDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class VilkårssettDTO(
    val kravfristVilkår: KravfristVilkårDTO,
    val kvpVilkår: KVPVilkårDTO,
    val introVilkår: IntroVilkårDTO,
    val institusjonsoppholdVilkår: InstitusjonsoppholdVilkårDTO,
    val livsoppholdVilkår: LivsoppholdVilkårDTO,
    val alderVilkår: AlderVilkårDTO,
)

internal fun Vilkårssett.toDTO(): VilkårssettDTO {
    return VilkårssettDTO(
        kravfristVilkår = kravfristVilkår.toDTO(),
        kvpVilkår = kvpVilkår.toDTO(),
        introVilkår = introVilkår.toDTO(),
        institusjonsoppholdVilkår = institusjonsoppholdVilkår.toDTO(),
        alderVilkår = alderVilkår.toDTO(),
        livsoppholdVilkår = livsoppholdVilkår.toDTO(),
    )
}
