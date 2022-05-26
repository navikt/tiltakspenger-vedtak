package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Utfallsperiode

interface Vilkår

interface FaktumVilkår<T> : Vilkår where T : Faktum {
    fun vurder(faktum: T, vurderingsperiode: Periode): UtfallsperioderForVilkår
}

object BrukerOppgittKVPVilkår : FaktumVilkår<BrukerOppgittKVPFaktum> {
    override fun vurder(faktum: BrukerOppgittKVPFaktum, vurderingsperiode: Periode): UtfallsperioderForVilkår {
        return UtfallsperioderForVilkår(
            this,
            listOf(
                Utfallsperiode(
                    utfall = if (faktum.deltarKVP) Utfall.VurdertOgIkkeOppfylt else Utfall.VurdertOgOppfylt,
                    periode = vurderingsperiode
                )
            )
        )
    }
}

object SaksbehandlerOppgittKVPVilkår : FaktumVilkår<SaksbehandlerOppgittKVPFaktum> {
    override fun vurder(faktum: SaksbehandlerOppgittKVPFaktum, vurderingsperiode: Periode): UtfallsperioderForVilkår {
        return UtfallsperioderForVilkår(
            this,
            listOf(
                Utfallsperiode(
                    utfall = if (faktum.deltarKVP) Utfall.VurdertOgIkkeOppfylt else Utfall.VurdertOgOppfylt,
                    periode = vurderingsperiode
                )
            )
        )
    }
}

object Over18Vilkår : FaktumVilkår<FødselsdatoFaktum> {
    override fun vurder(faktum: FødselsdatoFaktum, vurderingsperiode: Periode): UtfallsperioderForVilkår {
        return when {
            vurderingsperiode.etter(faktum.fødselsdato) ->
                UtfallsperioderForVilkår.utfallsperioderForVilkårBuilder(this)
                    .medUtfallFraOgMedTilOgMed(Utfall.VurdertOgOppfylt, vurderingsperiode).build()

            vurderingsperiode.før(faktum.fødselsdato) ->
                UtfallsperioderForVilkår.utfallsperioderForVilkårBuilder(this)
                    .medUtfallFraOgMedTilOgMed(Utfall.VurdertOgIkkeOppfylt, vurderingsperiode).build()

            else ->
                UtfallsperioderForVilkår.utfallsperioderForVilkårBuilder(this)
                    .medUtfallFraOgMedTilOgMed(
                        Utfall.VurdertOgIkkeOppfylt,
                        vurderingsperiode.fra,
                        faktum.fødselsdato.minusDays(1)
                    )
                    .utvidMedUtfallTilOgMed(Utfall.VurdertOgOppfylt, vurderingsperiode.til).build()
        }
    }
}

interface AkkumulertVilkår : Vilkår {
    fun akkumuler(utfallsperioderForVilkår: List<UtfallsperioderForVilkår>): UtfallsperioderForVilkår
}

object KVPVilkår : AkkumulertVilkår {
    override fun akkumuler(utfallsperioderForVilkår: List<UtfallsperioderForVilkår>): UtfallsperioderForVilkår {
        //Dette er ikke så enkelt..
        return utfallsperioderForVilkår.find { it.vilkår is SaksbehandlerOppgittKVPVilkår }
            ?.let { UtfallsperioderForVilkår(KVPVilkår, it.utfallsperioder) }
            ?: UtfallsperioderForVilkår(KVPVilkår, utfallsperioderForVilkår.first().utfallsperioder)
    }
}