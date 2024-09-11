package no.nav.tiltakspenger.utbetaling.domene

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.meldekort.domene.Meldeperiode

/**
 * Garanterer at utbetalingsperioden starter på en mandag, slutter på en søndag og varer i 14 dager.
 * Garanterer også at det ikke er duplikater, at den er sammenhengende og sortert.
 *
 * Det er avtalt mellom tiltakspenger, helved og OS/UR at vi ikke kan slå sammen utbetalingsdager på tvers av meldekortsperioder.
 */
data class UtbetalingsperioderGruppertPåMeldekortperiode(
    val utbetalingsperioder: NonEmptyList<Utbetalingsperiode>,
) : List<Utbetalingsperiode> by utbetalingsperioder {
    val meldekortId = utbetalingsperioder.first().meldekortId

    val fraOgMed = utbetalingsperioder.first().periode.fraOgMed
    val tilOgMed = utbetalingsperioder.last().periode.tilOgMed

    val periode: Periode = Periode(fraOgMed, tilOgMed)

    val beløp = utbetalingsperioder.sumOf { it.summertBeløp }

    val perioderSomSkalUtbetales = utbetalingsperioder.filterIsInstance<Utbetalingsperiode.SkalUtbetale>()

    init {
        require(periode.antallDager <= 14L) {
            "Utbetalingsperioder gruppert på meldekortperiode kan maks være 14 dager, men var ${periode.antallDager}"
        }
        utbetalingsperioder.zipWithNext { a, b ->
            require(a.periode.tilOgMed < b.periode.fraOgMed) {
                "Periodene kan ikke overlappe og må være sortert, men var ${utbetalingsperioder.map { it.periode }}"
            }
        }
        require(
            utbetalingsperioder.all {
                it.meldekortId == meldekortId
            },
        ) { "MeldekortId må være lik for alle utbetalingsperioder i en meldekortperiode" }
    }
}

fun Meldeperiode.UtfyltMeldeperiode.genererUtbetalingsperioderGruppertPåMeldekortperiode(): UtbetalingsperioderGruppertPåMeldekortperiode {
    val utbetalingsperioder =
        this.verdi
            .fold((listOf<Utbetalingsperiode>())) { acc, meldekortdag ->
                when (val sisteUtbetalingsperiode = acc.lastOrNull()) {
                    null -> meldekortdag.genererUtbetalingsperiode() ?.let { acc + it } ?: acc
                    else ->
                        sisteUtbetalingsperiode.leggTil(meldekortdag).let {
                            when (it) {
                                is Utbetalingsperiode.Resultat.KanIkkeSlåSammen -> {
                                    acc + meldekortdag.genererUtbetalingsperiode()!!
                                }
                                is Utbetalingsperiode.Resultat.KanSlåSammen -> {
                                    acc.dropLast(1) + it.utbetalingsperiode
                                }
                                is Utbetalingsperiode.Resultat.SkalIkkeUtbetales -> acc
                            }
                        }
                }
            }.toNonEmptyListOrNull()!!
    return UtbetalingsperioderGruppertPåMeldekortperiode(utbetalingsperioder)
}
