package no.nav.tiltakspenger.vilkårsvurdering

interface VilkårsvurderingKategori {
    fun lovreferanse(): Lovreferanse
    fun samletUtfall(): Utfall
    fun vurderinger(): List<Vurdering>
}
