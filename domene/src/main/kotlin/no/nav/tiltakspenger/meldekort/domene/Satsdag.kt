package no.nav.tiltakspenger.meldekort.domene

import java.time.LocalDate

data class Satsdag(
    val sats: Int,
    val satsRedusert: Int,
    val satsBarnetillegg: Int,
    val satsBarnetilleggRedusert: Int,
    val dato: LocalDate,
)
