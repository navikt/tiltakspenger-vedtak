package no.nav.tiltakspenger.vedtak.repository.felles

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2

internal data class PeriodisertUtfallDbJson(
    val utfall: UtfallDbJson,
    val periode: PeriodeDbJson,
) {
    enum class UtfallDbJson {
        OPPFYLT,
        IKKE_OPPFYLT,
        UAVKLART,
        ;

        fun toDomain(): Utfall2 {
            return when (this) {
                OPPFYLT -> Utfall2.OPPFYLT
                IKKE_OPPFYLT -> Utfall2.IKKE_OPPFYLT
                UAVKLART -> Utfall2.UAVKLART
            }
        }
    }
}

internal fun Periodisering<Utfall2>.toDbJson(): List<PeriodisertUtfallDbJson> {
    return this.perioder().map {
        PeriodisertUtfallDbJson(
            utfall = it.verdi.toDbJson(),
            periode = it.periode.toDbJson(),
        )
    }
}

internal fun Utfall2.toDbJson(): PeriodisertUtfallDbJson.UtfallDbJson {
    return when (this) {
        Utfall2.OPPFYLT -> PeriodisertUtfallDbJson.UtfallDbJson.OPPFYLT
        Utfall2.IKKE_OPPFYLT -> PeriodisertUtfallDbJson.UtfallDbJson.IKKE_OPPFYLT
        Utfall2.UAVKLART -> PeriodisertUtfallDbJson.UtfallDbJson.UAVKLART
    }
}

internal fun List<PeriodisertUtfallDbJson>.toDomain(): Periodisering<Utfall2> {
    return Periodisering(
        this.map {
            PeriodeMedVerdi(
                periode = it.periode.toDomain(),
                verdi = it.utfall.toDomain(),
            )
        },
    )
}
