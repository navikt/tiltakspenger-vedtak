package no.nav.tiltakspenger.vilkårsvurdering

class SykepengerVilkårsvurdering : StatligYtelseVilkårsvurdering() {
    override val ytelseVurderinger: List<Vurdering> = emptyList()
    override var manuellVurdering: Vurdering? = null
    override val lovreferanse: Lovreferanse = Lovreferanse.SYKEPENGER
}
