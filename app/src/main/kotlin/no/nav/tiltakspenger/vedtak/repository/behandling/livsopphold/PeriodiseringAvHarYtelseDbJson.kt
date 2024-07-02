package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.HarYtelse
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

internal data class PeriodiseringAvHarYtelseDbJson(
    val periode: PeriodeDbJson,
    val harYtelse: HarYtelseDbJson,
)

internal fun HarYtelse.toDbJson(): HarYtelseDbJson =
    when (this) {
        HarYtelse.HAR_YTELSE -> HarYtelseDbJson.HAR_YTELSE
        HarYtelse.HAR_IKKE_YTELSE -> HarYtelseDbJson.HAR_IKKE_YTELSE
    }

internal fun List<PeriodiseringAvHarYtelseDbJson>.toDomain(): Periodisering<HarYtelse> {
    return Periodisering(
        this.map {
            PeriodeMedVerdi(
                periode = it.periode.toDomain(),
                verdi = it.harYtelse.toDomain(),
            )
        },
    )
}

internal fun Periodisering<HarYtelse>.toDbJson(): List<PeriodiseringAvHarYtelseDbJson> {
    return this.perioder().map {
        PeriodiseringAvHarYtelseDbJson(
            periode = it.periode.toDbJson(),
            harYtelse = it.verdi.toDbJson(),
        )
    }
}
