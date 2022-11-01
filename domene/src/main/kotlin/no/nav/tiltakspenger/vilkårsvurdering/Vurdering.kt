package no.nav.tiltakspenger.vilkårsvurdering

import java.time.LocalDate

data class Vurdering(
    val vilkår: Vilkår,
    val kilde: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val utfall: Utfall,
    val detaljer: String,
)
