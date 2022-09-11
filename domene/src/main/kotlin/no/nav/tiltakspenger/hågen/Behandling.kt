package no.nav.tiltakspenger.hågen


// Noen hendelser vil ha effekt litt på kryss og tvers. Hvis man f.eks endrer tiltaksperioden, så må alle manuelle
// vurderinger gjøres på nytt. Kan håndteres vha en Observatør eller ved å la Eventen boble nedover overalt?

class Førstegangsbehandling(val samletVilkårsvurdering: SamletVilkårsvurdering) {


}

class Revurdering(var forrigeVurdering: SamletVilkårsvurdering, var endredeVilkårsvurderinger: List<Vilkårsvurdering>) {
    fun endreVurdering(vilkårsvurdering: Vilkårsvurdering) {}
}
