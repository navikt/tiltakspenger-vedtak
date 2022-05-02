package no.nav.tiltakspenger.domene

enum class Paragraf(beskrivelse: String) {
    PARAGRAF_3_LEDD_1_PUNKTUM1("Tiltakspenger kan gis til tiltaksdeltakere som har fylt 18 år"),
    PARAGRAF_3_LEDD_1_PUNKTUM3("Det gis et barnetillegg for hvert barn under 16 år som tiltaksdeltakeren forsørger."),
    PARAGRAF_7_LEDD_1_PUNKTUM1("")
}

interface Vilkår {
    val paragraf: Paragraf?
    fun sjekk(faktum: List<Faktum>): Boolean
}

object ErOver18År : Vilkår {
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1
    override fun sjekk(faktum: List<Faktum>): Boolean {
        when (faktum.first() is AldersFaktum) {
            true -> (faktum.first() as AldersFaktum).erOver18()
            else -> throw IllegalArgumentException("Wrong faktum")
        }
        return false
    }
}