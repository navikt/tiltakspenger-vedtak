package no.nav.tiltakspenger.domene

sealed class Utfall {
    class IkkeVurdert : Utfall()
    class VurdertOgOppfylt(val perioder: List<VurdertPeriode>) : Utfall()
    class VurdertOgIkkeOppfylt : Utfall()
    class VurdertOgTrengerManuellBehandling : Utfall()
}

class VurdertPeriode(
    val periode: Periode,
    val ikkeOppfylteVilkår: List<Vilkårsvurdering> = emptyList()
) {
    fun erOppfylt() = ikkeOppfylteVilkår.isEmpty()
}
