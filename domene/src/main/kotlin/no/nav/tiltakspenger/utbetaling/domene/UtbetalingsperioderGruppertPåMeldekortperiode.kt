package no.nav.tiltakspenger.utbetaling.domene

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.meldekort.domene.Meldekortperiode
import java.time.DayOfWeek

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
        require(periode.antallDager == 14L) {
            "Utbetalingsperioder gruppert på meldekortperiode må bestå av 14 dager, men var ${periode.antallDager}"
        }

        require(
            utbetalingsperioder
                .first()
                .periode.fraOgMed.dayOfWeek == DayOfWeek.MONDAY,
        ) { "Utbetalingsperiodene må starte på en mandag" }
        require(
            utbetalingsperioder
                .last()
                .periode.tilOgMed.dayOfWeek == DayOfWeek.SUNDAY,
        ) { "Utbetalingsperiodene må slutte på en sønad" }
        utbetalingsperioder.zipWithNext { a, b ->
            require(a.periode.tilOgMed.plusDays(1) == b.periode.fraOgMed) {
                "Datoene må være sammenhengende og sortert, men var ${utbetalingsperioder.map { it.periode }}"
            }
        }
        require(
            utbetalingsperioder.all {
                it.meldekortId == meldekortId
            },
        ) { "MeldekortId må være lik for alle utbetalingsperioder i en meldekortperiode" }
    }

    /**
     * TODO pre-mvp jah: Midlertidig løsning for route-DTO.
     * Vi kan ikke garantere at det brukes en enhetlig sats i meldekortperioden, siden meldekortet kan gå på tvers av satsendringav (1.januar).
     */
    fun satsUnsafe(): Sats = utbetalingsperioder.filterIsInstance<Utbetalingsperiode.SkalUtbetale>().first().sats
}

fun Meldekortperiode.genererUtbetalingsperioderGruppertPåMeldekortperiode(): UtbetalingsperioderGruppertPåMeldekortperiode {
    val utbetalingsperioder =
        this.verdi
            .fold((listOf<Utbetalingsperiode>())) { acc, meldekortdag ->
                when (val sisteUtbetalingsperiode = acc.lastOrNull()) {
                    null -> acc + meldekortdag.genererUtbetalingsperiode()
                    else ->
                        sisteUtbetalingsperiode.leggTil(meldekortdag).fold(
                            { acc + meldekortdag.genererUtbetalingsperiode() },
                            { acc.dropLast(1) + it },
                        )
                }
            }.toNonEmptyListOrNull()!!
    return UtbetalingsperioderGruppertPåMeldekortperiode(utbetalingsperioder)
}
