package no.nav.tiltakspenger.vilkårsvurdering

import java.time.LocalDate
import java.time.LocalDateTime

enum class VurderingType {
    AUTOMATISK,
    MANUELL,
}

data class Vurdering(
    val vilkår: Vilkår,
    val vurderingType: VurderingType,
    val kilde: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val utfall: Utfall,
    val detaljer: String,
    val tidspunkt: LocalDateTime = LocalDateTime.now()
)
