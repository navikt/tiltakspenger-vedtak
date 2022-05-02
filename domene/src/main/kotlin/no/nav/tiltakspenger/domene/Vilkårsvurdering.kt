package no.nav.tiltakspenger.domene

enum class Utfall {
    OPPFYLT,
    IKKE_OPPFYLT
}

class Vilkårsvurdering() {
    fun vurder(vilkår: List<Vilkår>): Utfall {
        vilkår.first().sjekk()
        return Utfall.IKKE_OPPFYLT
    }
}

val vurderinger: List<Vilkårsvurdering> = emptyList()
vurderinger.map { vurder() }