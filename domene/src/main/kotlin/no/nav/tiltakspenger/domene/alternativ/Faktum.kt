package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import java.time.LocalDate

interface Faktum {
    fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår
}

class SaksbehandlerOppgittKVPFaktum(val deltarKVP: Boolean) : Faktum {
    override fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår =
        SaksbehandlerOppgittKVPVilkår.vurder(this, vurderingsperiode)
}

interface BrukerOppgittKVPFaktum : Faktum {
    val deltarKVP: Boolean
    override fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår =
        BrukerOppgittKVPVilkår.vurder(this, vurderingsperiode)
}

class Søknad(override val deltarKVP: Boolean) : BrukerOppgittKVPFaktum

class FødselsdatoFaktum(val fødselsdato: LocalDate) : Faktum {
    override fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår =
        Over18Vilkår.vurder(this, vurderingsperiode)
}
