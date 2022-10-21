package no.nav.tiltakspenger.vilkårsvurdering

abstract class UtelukkendeManuellVurdering : Vilkårsvurdering() {
    override var manuellVurdering: Vurdering? = null
    override fun vurderinger(): List<Vurdering> = listOfNotNull(manuellVurdering)
    override fun detIkkeManuelleUtfallet(): Utfall = Utfall.KREVER_MANUELL_VURDERING
}
