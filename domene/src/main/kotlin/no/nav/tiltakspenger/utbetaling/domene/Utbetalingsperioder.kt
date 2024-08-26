package no.nav.tiltakspenger.utbetaling.domene

import arrow.core.NonEmptyList
import no.nav.tiltakspenger.libs.periodisering.Periode

/**
 * TODO pre-mvp jah: Skal brukes dersom vedtakene våre skal dekke hele vurderingsperioden eller evt. av korrigering.
 *
 * Garanterer at utbetalingsperiodene er sammenhengende og sorterte.
 * I tilfellet hvor bruker sender inn meldekort vil vi bare ha én periode, mens ved korrigering av meldekort tilbake i tid og omgjøring, vil en utbetaling spenne over flere meldekort.
 */
@Suppress("unused")
data class Utbetalingsperioder(
    val perioder: NonEmptyList<UtbetalingsperioderGruppertPåMeldekortperiode>,
) : List<UtbetalingsperioderGruppertPåMeldekortperiode> by perioder {
    val beløp = perioder.sumOf { it.beløp }

    val periode: Periode = Periode(perioder.first().periode.fraOgMed, perioder.last().periode.tilOgMed)

    init {
        perioder.zipWithNext { a, b ->
            require(a.periode.tilOgMed.plusDays(1) == b.periode.fraOgMed) {
                "Utbetalingsperiodene må være sammenhengende og sorterte, men var ${perioder.map { it.periode }}"
            }
        }
    }
}
