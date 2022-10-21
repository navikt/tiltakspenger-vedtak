package no.nav.tiltakspenger.vilkårsvurdering

import java.time.LocalDate

data class Vurdering(
    val lovreferanse: Lovreferanse,
    val kilde: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val utfall: Utfall,
    val detaljer: String,
)
