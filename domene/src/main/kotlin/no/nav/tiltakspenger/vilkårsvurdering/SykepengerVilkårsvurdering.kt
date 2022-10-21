package no.nav.tiltakspenger.vilkårsvurdering

class SykepengerVilkårsvurdering : UtelukkendeManuellVurdering() {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.SYKEPENGER
}
