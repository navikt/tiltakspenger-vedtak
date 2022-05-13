package no.nav.tiltakspenger.domene

sealed class Utfall {
    class IkkeVurdert : Utfall()
    class VurdertOgOppfylt(val vilkårOppfyltPeriode: Periode) : Utfall()
    class VurdertOgIkkeOppfylt : Utfall()
    class VurdertOgTrengerManuellBehandling : Utfall()
}

data class Vilkårsvurdering(
    val utfall: Utfall = Utfall.IkkeVurdert(),
    val vilkår: Vilkår,
    val fakta: List<Faktum> = emptyList(),
    val vurderingsperiode: Periode
) {
    fun vurder(faktum: Faktum): Vilkårsvurdering {
        val oppdaterteFakta = fakta + listOf(faktum).filter { faktum -> faktum.erRelevantFor(vilkår) }
        return this.copy(
            utfall = vilkår.vurder(oppdaterteFakta, vurderingsperiode),
            fakta = oppdaterteFakta,
        )
    }
}

fun List<Vilkårsvurdering>.erInngangsVilkårOppfylt(): Boolean = this
    .filter { it.vilkår.erInngangsVilkår }
    .all { it.utfall is Utfall.VurdertOgOppfylt }
