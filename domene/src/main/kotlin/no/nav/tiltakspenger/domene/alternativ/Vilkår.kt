package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Utfallsperiode

interface Vilkår

interface FaktumVilkår<T> : Vilkår where T : Faktum {
    fun vurder(faktum: T, vurderingsperiode: Periode): UtfallsperioderForVilkår
}

object KVPVilkår : FaktumVilkår<KVPFaktum> {
    override fun vurder(faktum: KVPFaktum, vurderingsperiode: Periode): UtfallsperioderForVilkår {
        return UtfallsperioderForVilkår(
            this,
            listOf(Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, periode = vurderingsperiode))
        )
    }
}

object Over18Vilkår : FaktumVilkår<Over18Faktum> {
    override fun vurder(faktum: Over18Faktum, vurderingsperiode: Periode): UtfallsperioderForVilkår {
        return UtfallsperioderForVilkår(
            this,
            listOf(Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, periode = vurderingsperiode))
        )
    }
}

interface AkkumulertVilkår : Vilkår {
    fun akkumuler(utfallsperioderForVilkår: List<UtfallsperioderForVilkår>): UtfallsperioderForVilkår
}

object LivsoppholdsytelserVilkår : AkkumulertVilkår {
    override fun akkumuler(utfallsperioderForVilkår: List<UtfallsperioderForVilkår>): UtfallsperioderForVilkår {
        return utfallsperioderForVilkår.first()
    }
}