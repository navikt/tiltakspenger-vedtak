package no.nav.tiltakspenger.meldekort.domene

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Fra paragraf 5: Enhver som mottar tiltakspenger, må som hovedregel melde seg til Arbeids- og velferdsetaten hver fjortende dag (meldeperioden)
 */
sealed interface Meldeperiode : List<Meldekortdag> {
    val fraOgMed: LocalDate get() = this.first().dato
    val tilOgMed: LocalDate get() = this.last().dato
    val periode: Periode get() = Periode(fraOgMed, tilOgMed)
    val sakId: SakId
    val tiltakstype: TiltakstypeSomGirRett
    val meldekortId: MeldekortId

    /**
     * @property tiltakstype I MVP støtter vi kun ett tiltak, men på sikt kan vi ikke garantere at det er én til én mellom meldekortperiode og tiltakstype.
     */
    data class UtfyltMeldeperiode(
        override val sakId: SakId,
        val verdi: NonEmptyList<Meldekortdag.Utfylt>,
    ) : Meldeperiode,
        List<Meldekortdag> by verdi {
        override val tiltakstype: TiltakstypeSomGirRett = verdi.first().tiltakstype
        override val meldekortId = verdi.first().meldekortId

        init {
            require(verdi.size == 14) { "En meldekortperiode må være 14 dager, men var ${verdi.size}" }
            require(verdi.first().dato.dayOfWeek == DayOfWeek.MONDAY) { "Utbetalingsperioden må starte på en mandag" }
            require(verdi.last().dato.dayOfWeek == DayOfWeek.SUNDAY) { "Utbetalingsperioden må slutte på en søndag" }
            verdi.forEachIndexed { index, dag ->
                require(verdi.first().dato.plusDays(index.toLong()) == dag.dato) {
                    "Datoene må være sammenhengende og sortert, men var ${verdi.map { it.dato }}"
                }
            }
            // TODO post-mvp jah: Vurder om Meldekortperiode skal forholde seg til antallDager eller om fordeleren [Meldekort] skal gjøre det.
            require(verdi.count { it.harDeltattEllerFravær } <= 5 * 2) {
                // TODO jah: Dette bør være en mer felles konstant. Kan også skrive en mer spesifikk sjekk per uke.
                "Det kan maks være 5*2 tiltaksdager i en meldekortperiode, men var ${verdi.count { it.harDeltattEllerFravær }}"
            }
            require(verdi.all { it.tiltakstype == tiltakstype }) {
                "Alle tiltaksdager må ha samme tiltakstype som meldekortsperioden: $tiltakstype. Dager: ${verdi.map { it.tiltakstype }}"
            }
            require(
                verdi.all { it.meldekortId == meldekortId },
            ) { "Alle dager må tilhøre samme meldekort, men var: ${verdi.map { it.meldekortId }}" }
        }
        fun beregnTotalbeløp(): Int = verdi.sumOf { it.beregningsdag?.beløp ?: 0 }
    }

    /**
     * Merk at ikke utfylt betyr at ikke alle dager utfylt. Noen dager kan være Sperret, og de anses som utfylt.
     *
     *  @property tiltakstype I MVP støtter vi kun ett tiltak, men på sikt kan vi ikke garantere at det er én til én mellom meldekortperiode og tiltakstype.
     */
    data class IkkeUtfyltMeldeperiode(
        override val sakId: SakId,
        val verdi: NonEmptyList<Meldekortdag>,
    ) : Meldeperiode,
        List<Meldekortdag> by verdi {
        override val tiltakstype = verdi.first().tiltakstype

        override val meldekortId = verdi.first().meldekortId

        companion object {
            /**
             * @param meldeperiode Perioden meldekortet skal gjelde for. Må være 14 dager, starte på en mandag og slutte på en søndag.
             * @param utfallsperioder Knyttet til vedtaket. Hvilke dager/perioder kan bruker få ytelse? Hvis denne ikke overlapper med [meldeperiode] vil alle dagene bli SPERRET.
             *
             * @return Meldekortperiode som er utfylt.
             * @throws IllegalStateException Dersom alle dagene i en meldekortperiode er SPERRET er den per definisjon utfylt. Dette har vi ikke støtte for i MVP.
             */
            fun fraPeriode(
                meldeperiode: Periode,
                utfallsperioder: Periodisering<AvklartUtfallForPeriode>,
                tiltakstype: TiltakstypeSomGirRett,
                meldekortId: MeldekortId,
                sakId: SakId,
            ): IkkeUtfyltMeldeperiode {
                val dager =
                    meldeperiode.tilDager().map { dag ->
                        if (utfallsperioder.hentVerdiForDag(dag) == AvklartUtfallForPeriode.OPPFYLT) {
                            Meldekortdag.IkkeUtfylt(
                                dato = dag,
                                meldekortId = meldekortId,
                                tiltakstype = tiltakstype,
                            )
                        } else {
                            Meldekortdag.Utfylt.Sperret(
                                dato = dag,
                                meldekortId = meldekortId,
                                tiltakstype = tiltakstype,
                            )
                        }
                    }
                return if (dager.any { it is Meldekortdag.IkkeUtfylt }) {
                    IkkeUtfyltMeldeperiode(sakId, dager.toNonEmptyListOrNull()!!)
                } else {
                    throw IllegalStateException("Alle dagene i en meldekortperiode er SPERRET. Dette har vi ikke støtte for i MVP.")
                }
            }
        }

        init {
            require(verdi.size == 14) { "En meldekortperiode må være 14 dager, men var ${verdi.size}" }
            require(verdi.first().dato.dayOfWeek == DayOfWeek.MONDAY) { "Utbetalingsperioden må starte på en mandag" }
            require(verdi.last().dato.dayOfWeek == DayOfWeek.SUNDAY) { "Utbetalingsperioden må slutte på en søndag" }
            verdi.forEachIndexed { index, dag ->
                require(verdi.first().dato.plusDays(index.toLong()) == dag.dato) {
                    "Datoene må være sammenhengende og sortert, men var ${verdi.map { it.dato }}"
                }
            }

            require(verdi.all { it.tiltakstype == tiltakstype }) {
                "Alle tiltaksdager må ha samme tiltakstype som meldekortsperioden: $tiltakstype. Dager: ${verdi.map { it.tiltakstype }}"
            }
            require(
                verdi.all { it.meldekortId == meldekortId },
            ) { "Alle dager må tilhøre samme meldekort, men var: ${verdi.map { it.meldekortId }}" }
            require(
                verdi.all { it is Meldekortdag.IkkeUtfylt || it is Meldekortdag.Utfylt.Sperret },
                { "Alle dagene må være av typen Ikke Utfylt eller Sperret." },
            )
        }
    }
}
