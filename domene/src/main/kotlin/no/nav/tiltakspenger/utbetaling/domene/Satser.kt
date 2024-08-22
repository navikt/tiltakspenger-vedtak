package no.nav.tiltakspenger.utbetaling.domene

import no.nav.tiltakspenger.felles.desember
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.libs.periodisering.Periode
import java.time.LocalDate

class Satser {
    companion object {
        private val satser =
            listOf(
                Sats(Periode(1.januar(2023), 31.desember(2023)), 268, 201, 52, 39),
                Sats(Periode(1.januar(2024), 31.desember(9999)), 285, 214, 53, 40),
            )

        fun sats(date: LocalDate): Sats =
            satser.find { it.fraOgMed <= date && it.tilOgMed >= date }
                ?: throw IllegalArgumentException("Fant ingen sats for dato $date")
    }
}

data class Sats(
    val periode: Periode,
    val sats: Int,
    val satsDelvis: Int,
    val satsBarnetillegg: Int,
    val satsBarnetilleggDelvis: Int,
) {
    val fraOgMed = periode.fraOgMed
    val tilOgMed = periode.tilOgMed

    init {
        require(sats > 0) { "Sats må være positiv, men var $sats" }
        require(satsDelvis > 0) { "Sats delvis må være positiv, men var $satsDelvis" }
        require(satsBarnetillegg > 0) { "Sats barnetillegg må være positiv, men var $satsBarnetillegg" }
        require(satsBarnetilleggDelvis > 0) { "Sats barnetillegg delvis må være, men var $satsBarnetilleggDelvis" }
    }
}
