package no.nav.tiltakspenger.domene.vilkår.temp

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.vilkår.Utfall
import no.nav.tiltakspenger.domene.vilkår.Vilkår

data class Vurdering(
    val vilkår: Vilkår,
    val kilde: Kilde,
    val utfall: Utfall,
) : Sammenlignbar<Vurdering> {
    override fun sammenlignbareFelter(): Set<*> =
        setOf(this.vilkår, this.kilde, this.utfall)
}
