package no.nav.tiltakspenger.vedtak.repository.meldekort

import no.nav.tiltakspenger.meldekort.domene.Beregningsdag
import java.time.LocalDate

data class BeregningsdagDbJson(
    val beløp: Int,
    val prosent: Int,
    val satsdag: SatsdagDbJson,
    val dato: LocalDate,
)

fun BeregningsdagDbJson.toBeregningsdag(): Beregningsdag = Beregningsdag(beløp = beløp, prosent = prosent, satsdag = satsdag.toSatsdag(), dato = dato)

fun Beregningsdag.toDbJson(): BeregningsdagDbJson =
    BeregningsdagDbJson(beløp = beløp, prosent = prosent, satsdag = satsdag.toDbJson(), dato = dato)
