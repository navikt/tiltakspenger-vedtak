package no.nav.tiltakspenger.vedtak.repository.utbetaling

import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsdag
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsperiode
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingsperioderGruppertPåMeldekortperiode
import no.nav.tiltakspenger.vedtak.db.deserializeList
import no.nav.tiltakspenger.vedtak.db.serialize
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.meldekort.BeregningsdagDbJson
import no.nav.tiltakspenger.vedtak.repository.meldekort.toBeregningsdag
import no.nav.tiltakspenger.vedtak.repository.meldekort.toDbJson
import no.nav.tiltakspenger.vedtak.repository.sats.toDbJson
import no.nav.tiltakspenger.vedtak.repository.tiltak.toTiltakstypeSomGirRett
import java.time.LocalDate

/**
 * @property utbetalingsdager er tom dersom [skalUtbetale] er false.
 */
private data class UtbetalingsperiodeDbJson(
    val meldekortId: String,
    val periode: PeriodeDbJson,
    val utbetalingsdager: List<UtbetalingsdagDbJson>,
    val skalUtbetale: Boolean,
)

private data class UtbetalingsdagDbJson(
    val dato: LocalDate,
    val meldekortId: String,
    val beregningsdag: BeregningsdagDbJson,
    val tiltakstype: String,
    val beløp: Int,
    val status: Status,
) {
    enum class Status {
        FullUtbetaling,
        DelvisUtbetaling,
        ;

        fun toDomain(): Utbetalingsdag.Status {
            return when (this) {
                FullUtbetaling -> Utbetalingsdag.Status.FullUtbetaling
                DelvisUtbetaling -> Utbetalingsdag.Status.DelvisUtbetaling
            }
        }
    }
}

private fun String.toUtbetalingsdagStatus(): Utbetalingsdag.Status {
    return UtbetalingsdagDbJson.Status.valueOf(this).toDomain()
}

fun String.toUtbetalingsperiode(): UtbetalingsperioderGruppertPåMeldekortperiode {
    val utbetalingsperioder: List<UtbetalingsperiodeDbJson> = deserializeList<UtbetalingsperiodeDbJson>()
    return UtbetalingsperioderGruppertPåMeldekortperiode(
        utbetalingsperioder.map { utbetalingsperiode ->
            if (utbetalingsperiode.skalUtbetale) {
                Utbetalingsperiode.SkalUtbetale(
                    utbetalingsperiode.utbetalingsdager.map { utbetalingsdag ->
                        Utbetalingsdag(
                            dato = utbetalingsdag.dato,
                            meldekortId = MeldekortId.fromString(utbetalingsdag.meldekortId),
                            beregningsdag = utbetalingsdag.beregningsdag.toBeregningsdag(),
                            tiltakstype = utbetalingsdag.tiltakstype.toTiltakstypeSomGirRett(),
                            status = utbetalingsdag.status.toDomain(),
                        )
                    }.toNonEmptyListOrNull()!!,
                )
            } else {
                Utbetalingsperiode.SkalIkkeUtbetale(
                    periode = utbetalingsperiode.periode.toDomain(),
                    meldekortId = MeldekortId.fromString(utbetalingsperiode.meldekortId),
                )
            }
        }.toNonEmptyListOrNull()!!,
    )
}

fun UtbetalingsperioderGruppertPåMeldekortperiode.toDbJson(): String {
    return this.utbetalingsperioder.map { utbetalingsperiode ->
        UtbetalingsperiodeDbJson(
            meldekortId = utbetalingsperiode.meldekortId.toString(),
            periode = utbetalingsperiode.periode.toDbJson(),
            utbetalingsdager = when (utbetalingsperiode) {
                is Utbetalingsperiode.SkalUtbetale -> utbetalingsperiode.utbetalingsperiode.map { utbetalingsdag ->
                    UtbetalingsdagDbJson(
                        dato = utbetalingsdag.dato,
                        meldekortId = utbetalingsdag.meldekortId.toString(),
                        beregningsdag = utbetalingsdag.beregningsdag.toDbJson(),
                        tiltakstype = utbetalingsdag.tiltakstype.name,
                        beløp = utbetalingsdag.beløp,
                        status = when (utbetalingsdag.status) {
                            Utbetalingsdag.Status.FullUtbetaling -> UtbetalingsdagDbJson.Status.FullUtbetaling
                            Utbetalingsdag.Status.DelvisUtbetaling -> UtbetalingsdagDbJson.Status.DelvisUtbetaling
                        },
                    )
                }

                is Utbetalingsperiode.SkalIkkeUtbetale -> emptyList()
            },
            skalUtbetale = utbetalingsperiode is Utbetalingsperiode.SkalUtbetale,
        )
    }.let {
        serialize(it)
    }
}
