package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.fakta.FaktumKilde
import java.time.LocalDate

interface Faktum {
    fun kilde(): FaktumKilde
    fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår
}

interface BrukerOppgittFaktum : Faktum {
    override fun kilde() = FaktumKilde.BRUKER
}

interface SaksbehandlerOppgittFaktum : Faktum {
    override fun kilde() = FaktumKilde.SAKSBEHANDLER
}

interface RegisterFaktum : Faktum {
    override fun kilde() = FaktumKilde.SYSTEM
}

class SaksbehandlerOppgittKVPFaktum(val deltarKVP: Boolean) : SaksbehandlerOppgittFaktum {
    override fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår =
        SaksbehandlerOppgittKVPVilkår.vurder(this, vurderingsperiode)
}

interface BrukerOppgittKVPFaktum : BrukerOppgittFaktum {
    val deltarKVP: Boolean
    override fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår =
        BrukerOppgittKVPVilkår.vurder(this, vurderingsperiode)
}

class Søknad(override val deltarKVP: Boolean) : BrukerOppgittKVPFaktum

class FødselsdatoFaktum(val fødselsdato: LocalDate) : RegisterFaktum {
    override fun vurderFor(vurderingsperiode: Periode): UtfallsperioderForVilkår =
        Over18Vilkår.vurder(this, vurderingsperiode)
}
