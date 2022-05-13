package no.nav.tiltakspenger.domene

import java.time.LocalDate
import java.time.LocalDate.now

enum class Paragraf(beskrivelse: String) {
    PARAGRAF_3_LEDD_1_PUNKTUM1("Tiltakspenger kan gis til tiltaksdeltakere som har fylt 18 år"),
    PARAGRAF_3_LEDD_1_PUNKTUM3("Det gis et barnetillegg for hvert barn under 16 år som tiltaksdeltakeren forsørger."),
    PARAGRAF_7_LEDD_2_PUNKTUM1("KVP")
}

//fun Faktum.erRelevantFor(vilkår: Vilkår): Boolean = vilkår.relevanteFaktaTyper.any { relevantType -> relevantType.isInstance(this)  }

interface Vilkår {
    val erInngangsVilkår: Boolean
    val paragraf: Paragraf?

    //    val relevanteFaktaTyper: List<KClass<out Faktum>>
    fun vurder(faktum: List<Faktum>): Utfall
}

object ErOver18År : Vilkår {
    //    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(AldersFaktum::class)
    val fødselsdato: LocalDate = now()
    override val erInngangsVilkår: Boolean = true
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1

    override fun vurder(faktum: List<Faktum>): Utfall {
        if (faktum?.firstOrNull() !is AldersFaktum) return Utfall.IKKE_VURDERT
        return vurder((faktum.first() as AldersFaktum))
    }

    private fun vurder(faktum: AldersFaktum): Utfall {
        return when (faktum.erOver18()) {
            true -> Utfall.VURDERT_OG_OPPFYLT
            else -> Utfall.VURDERT_OG_IKKE_OPPFYLT
        }
    }
}

class KVP {
    fun håndterKVPFraSøknad() {}
    fun håndterKVPFraSystem() {}
}

val inngangsVilkår = listOf<Vilkår>(ErOver18År)
