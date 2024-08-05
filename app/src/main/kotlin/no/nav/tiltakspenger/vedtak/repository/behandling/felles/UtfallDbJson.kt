package no.nav.tiltakspenger.vedtak.repository.behandling.felles

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.UtfallForPeriode
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

internal data class PeriodisertUtfallDbJson(
    val utfall: UtfallDbJson,
    val periode: PeriodeDbJson,
) {
    enum class UtfallDbJson {
        OPPFYLT,
        IKKE_OPPFYLT,
        UAVKLART,
        ;

        fun toDomain(): UtfallForPeriode {
            return when (this) {
                OPPFYLT -> UtfallForPeriode.OPPFYLT
                IKKE_OPPFYLT -> UtfallForPeriode.IKKE_OPPFYLT
                UAVKLART -> UtfallForPeriode.UAVKLART
            }
        }
    }
}

internal fun Periodisering<UtfallForPeriode>.toDbJson(): List<PeriodisertUtfallDbJson> {
    return this.perioder().map {
        PeriodisertUtfallDbJson(
            utfall = it.verdi.toDbJson(),
            periode = it.periode.toDbJson(),
        )
    }
}

internal fun UtfallForPeriode.toDbJson(): PeriodisertUtfallDbJson.UtfallDbJson {
    return when (this) {
        UtfallForPeriode.OPPFYLT -> PeriodisertUtfallDbJson.UtfallDbJson.OPPFYLT
        UtfallForPeriode.IKKE_OPPFYLT -> PeriodisertUtfallDbJson.UtfallDbJson.IKKE_OPPFYLT
        UtfallForPeriode.UAVKLART -> PeriodisertUtfallDbJson.UtfallDbJson.UAVKLART
    }
}

internal fun List<PeriodisertUtfallDbJson>.toDomain(): Periodisering<UtfallForPeriode> {
    return Periodisering(
        this.map {
            PeriodeMedVerdi(
                periode = it.periode.toDomain(),
                verdi = it.utfall.toDomain(),
            )
        },
    )
}
