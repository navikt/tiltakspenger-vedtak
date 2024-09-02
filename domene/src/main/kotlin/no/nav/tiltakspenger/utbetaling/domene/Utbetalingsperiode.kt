package no.nav.tiltakspenger.utbetaling.domene

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag

/**
 * Vi slår sammen utbetalingsdager som er like og sammenhengende.
 * Garanterer også at det ikke er duplikater, at den er sammenhengende og sortert.
 */
sealed interface Utbetalingsperiode {
    val periode: Periode
    val meldekortId: MeldekortId
    val summertBeløp: Int
    val beløpPerDag: Int

    fun leggTil(meldekortdag: Meldekortdag): Either<KanIkkeSlåSammen, Utbetalingsperiode>

    object KanIkkeSlåSammen

    data class SkalUtbetale(
        val utbetalingsperiode: NonEmptyList<Utbetalingsdag>,
    ) : Utbetalingsperiode,
        List<Utbetalingsdag> by utbetalingsperiode {
        override val meldekortId = utbetalingsperiode.first().meldekortId
        val fraOgMed = utbetalingsperiode.first().dato
        val tilOgMed = utbetalingsperiode.last().dato
        override val periode = Periode(fraOgMed, tilOgMed)
        override val summertBeløp = utbetalingsperiode.sumOf { it.beløp }
        override val beløpPerDag = utbetalingsperiode.first().beløp
        val tiltakstype: TiltakstypeSomGirRett = utbetalingsperiode.first().tiltakstype
        val sats: Sats = utbetalingsperiode.first().sats

        init {
            require(
                utbetalingsperiode
                    .map { it.dato }
                    .toSet()
                    .size == utbetalingsperiode.size,
            ) { "Datoer må være unike" }
            utbetalingsperiode.zipWithNext { a, b ->
                require(a.dato.plusDays(1) == b.dato) {
                    "Datoene må være sammenhengende og sortert, men var ${utbetalingsperiode.map { it.dato }}"
                }
            }
            require(
                utbetalingsperiode.all {
                    it.meldekortId == meldekortId
                },
            ) {
                "MeldekortId må være lik for alle utbetalingsdager i en periode, men var ${utbetalingsperiode.map {
                    it.meldekortId
                }}"
            }
            require(
                utbetalingsperiode.all { it.beløp == beløpPerDag },
            ) { "Beløp må være lik for alle utbetalingsdager i en utbetalingsperiode" }
            require(
                utbetalingsperiode.all {
                    it.tiltakstype == tiltakstype
                },
            ) { "Tiltakstype må være lik for alle utbetalingsdager i en utbetalingsperiode" }
            require(utbetalingsperiode.all { it.sats == sats }) { "Sats må være lik for alle utbetalingsdager i en utbetalingsperiode" }
        }

        override fun leggTil(meldekortdag: Meldekortdag): Either<KanIkkeSlåSammen, Utbetalingsperiode> {
            if (meldekortdag !is Meldekortdag.Tiltaksdag) {
                return KanIkkeSlåSammen.left()
            }
            return if (kanSlåSammen(meldekortdag)) {
                SkalUtbetale(utbetalingsperiode.plus(meldekortdag.genererUtbetalingsdag())).right()
            } else {
                KanIkkeSlåSammen.left()
            }
        }

        private fun kanSlåSammen(annen: Meldekortdag): Boolean =
            this.last().kanSlåSammen(annen) && this.last().dato.plusDays(1) == annen.dato
    }

    data class SkalIkkeUtbetale(
        override val periode: Periode,
        override val meldekortId: MeldekortId,
    ) : Utbetalingsperiode {
        override val summertBeløp = 0
        override val beløpPerDag = 0

        override fun leggTil(meldekortdag: Meldekortdag): Either<KanIkkeSlåSammen, Utbetalingsperiode> =
            if (kanSlåSammen(meldekortdag)) {
                SkalIkkeUtbetale(Periode(periode.fraOgMed, meldekortdag.dato), meldekortId).right()
            } else {
                KanIkkeSlåSammen.left()
            }

        private fun kanSlåSammen(annen: Meldekortdag): Boolean =
            when (annen) {
                is Meldekortdag.IkkeTiltaksdag ->
                    this.meldekortId == annen.meldekortId &&
                        this.periode.tilOgMed.plusDays(1) == annen.dato

                is Meldekortdag.Tiltaksdag -> false
            }
    }
}

fun Meldekortdag.genererUtbetalingsperiode(): Utbetalingsperiode =
    when (this) {
        // TODO pre-mvp jah: En tiltaksdag kan ende opp i at vi ikke skal utbetale. Syk/fravær osv.
        is Meldekortdag.Tiltaksdag ->
            Utbetalingsperiode.SkalUtbetale(
                nonEmptyListOf(this.genererUtbetalingsdag()),
            )

        is Meldekortdag.IkkeTiltaksdag ->
            Utbetalingsperiode.SkalIkkeUtbetale(
                Periode(dato, dato),
                meldekortId,
            )
    }
