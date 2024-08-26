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
import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær

/**
 * Vi slår sammen utbetalingsdager som er like og sammenhengende.
 * Garanterer også at det ikke er duplikater, at den er sammenhengende og sortert.
 */
sealed interface Utbetalingsperiode {
    val periode: Periode
    val meldekortId: MeldekortId
    val summertBeløp: Int
    val beløpPerDag: Int

    fun leggTil(meldekortdag: Meldekortdag.Utfylt): Either<KanIkkeSlåSammen, Utbetalingsperiode>

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
                "MeldekortId må være lik for alle utbetalingsdager i en periode, men var ${
                    utbetalingsperiode.map {
                        it.meldekortId
                    }
                }"
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

        override fun leggTil(meldekortdag: Meldekortdag.Utfylt): Either<KanIkkeSlåSammen, Utbetalingsperiode> {
            return if (kanSlåSammen(meldekortdag)) {
                SkalUtbetale(utbetalingsperiode.plus(meldekortdag.genererUtbetalingsdag())).right()
            } else {
                KanIkkeSlåSammen.left()
            }
        }

        private fun kanSlåSammen(neste: Meldekortdag.Utfylt): Boolean =
            this.last().kanSlåSammen(neste)
    }

    /**
     * Vi sender ikke disse periodene til helved.
     * TODO pre-mvp jah: Diskuter med teamet om vi ønsker å ha dette i modellen vår/persistere.
     */
    data class SkalIkkeUtbetale(
        override val periode: Periode,
        override val meldekortId: MeldekortId,
    ) : Utbetalingsperiode {
        override val summertBeløp = 0
        override val beløpPerDag = 0

        override fun leggTil(meldekortdag: Meldekortdag.Utfylt): Either<KanIkkeSlåSammen, Utbetalingsperiode> =
            if (kanSlåSammen(meldekortdag)) {
                SkalIkkeUtbetale(Periode(periode.fraOgMed, meldekortdag.dato), meldekortId).right()
            } else {
                KanIkkeSlåSammen.left()
            }

        private fun kanSlåSammen(neste: Meldekortdag.Utfylt): Boolean {
            return when (neste.reduksjon) {
                ReduksjonAvYtelsePåGrunnAvFravær.YtelsenFallerBort ->
                    this.meldekortId == neste.meldekortId &&
                        this.periode.tilOgMed.plusDays(1) == neste.dato

                ReduksjonAvYtelsePåGrunnAvFravær.IngenReduksjon, ReduksjonAvYtelsePåGrunnAvFravær.DelvisReduksjon -> false
            }
        }
    }
}

fun Meldekortdag.Utfylt.genererUtbetalingsperiode(): Utbetalingsperiode =
    when (this.reduksjon) {
        ReduksjonAvYtelsePåGrunnAvFravær.IngenReduksjon, ReduksjonAvYtelsePåGrunnAvFravær.DelvisReduksjon ->
            Utbetalingsperiode.SkalUtbetale(
                nonEmptyListOf(this.genererUtbetalingsdag()),
            )

        ReduksjonAvYtelsePåGrunnAvFravær.YtelsenFallerBort ->
            Utbetalingsperiode.SkalIkkeUtbetale(
                Periode(dato, dato),
                meldekortId,
            )
    }
