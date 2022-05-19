package no.nav.tiltakspenger.domene

/*
sealed class VilkårsVurderingSealed(
    val vilkår: Vilkår,
    val id: UUID = UUID.randomUUID(),
) {

    class IkkeVurdertVilkår(vilkår: Vilkår) : VilkårsVurderingSealed(vilkår = vilkår) {
        fun vurder(): VilkårsVurderingSealed {
            return VurdertVilkår(
                utfall = Utfall.IkkeVurdert(),
                vilkår = vilkår,
            )
        }
    }

    class VurdertVilkår(
        val utfall: Utfall,
        vilkår: Vilkår
    ) : VilkårsVurderingSealed(vilkår = vilkår)

}

fun gjørNoeMedVilkår(vilkårsVurderingSealed: VilkårsVurderingSealed) {
    when (val vilk = vilkårsVurderingSealed) {
        is VilkårsVurderingSealed.VurdertVilkår -> vilk.utfall
        is VilkårsVurderingSealed.IkkeVurdertVilkår -> TODO()
    }
}

 */
