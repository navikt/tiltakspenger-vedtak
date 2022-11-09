package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

abstract class KommunalYtelseVilkårsvurdering(
    override var vurderinger: List<Vurdering>
) : Vilkårsvurdering() {
    companion object {
        const val KILDE = "Søknad"
    }
}
