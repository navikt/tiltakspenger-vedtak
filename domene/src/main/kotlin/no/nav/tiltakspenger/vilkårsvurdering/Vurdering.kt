package no.nav.tiltakspenger.vilkårsvurdering

import java.time.LocalDate

data class Vurdering(
    val kilde: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val utfall: Utfall,
)
