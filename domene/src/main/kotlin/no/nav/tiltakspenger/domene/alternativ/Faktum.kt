package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import java.time.LocalDate

interface Faktum {
    fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår
}

class KVPFaktum(private val deltarKVP: Boolean) : Faktum {
    override fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår =
        KVPVilkår.vurder(this, vurderingsperiode)
}

class Over18Faktum(private val fdato: LocalDate) : Faktum {
    override fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår =
        Over18Vilkår.vurder(this, vurderingsperiode)
}