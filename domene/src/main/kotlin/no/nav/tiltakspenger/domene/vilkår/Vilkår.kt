package no.nav.tiltakspenger.domene.vilkår

import KVP
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfallsperiode
import no.nav.tiltakspenger.domene.fakta.Faktum
import kotlin.reflect.KClass

enum class Paragraf(beskrivelse: String) {
    PARAGRAF_3_LEDD_1_PUNKTUM1("Tiltakspenger kan gis til tiltaksdeltakere som har fylt 18 år"),
    PARAGRAF_3_LEDD_1_PUNKTUM3("Det gis et barnetillegg for hvert barn under 16 år som tiltaksdeltakeren forsørger."),
    PARAGRAF_7_LEDD_2_PUNKTUM1("KVP.kt")
}

fun Faktum.erRelevantFor(vilkår: Vilkår): Boolean =
    vilkår.relevanteFaktaTyper.any { relevantType -> relevantType.isInstance(this) }

interface Vilkår {
    val erInngangsVilkår: Boolean
    val paragraf: Paragraf?

    val relevanteFaktaTyper: List<KClass<out Faktum>>
    fun vurder(faktum: List<Faktum>, vurderingsperiode: Periode): List<Utfallsperiode>
}


val inngangsVilkår = listOf(ErOver18År, KVP)
