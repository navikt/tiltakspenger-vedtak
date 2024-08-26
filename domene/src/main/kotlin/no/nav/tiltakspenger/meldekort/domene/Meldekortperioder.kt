package no.nav.tiltakspenger.meldekort.domene

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett

/**
 * Består av én eller flere [Meldekortperiode]
 * @param tiltakstype I MVP støtter vi kun ett tiltak, men på sikt kan vi ikke garantere at det er én til én mellom meldekortperioder og tiltakstype.
 */
data class Meldekortperioder(
    val meldekortperioder: NonEmptyList<Meldekortperiode>,
    val tiltakstype: TiltakstypeSomGirRett,
) : List<Meldekortperiode> by meldekortperioder {

    constructor(meldekortperiode: Meldekortperiode, tiltakstype: TiltakstypeSomGirRett) : this(
        nonEmptyListOf(
            meldekortperiode,
        ),
        tiltakstype,
    )

    val periode: Periode = Periode(meldekortperioder.first().fraOgMed, meldekortperioder.last().tilOgMed)

    init {
        meldekortperioder.zipWithNext { a, b ->
            require(a.tilOgMed.plusDays(1) == b.fraOgMed) {
                "Meldekortperiodene må være sammenhengende og sortert, men var ${meldekortperioder.map { it.periode }}"
            }
        }
        require(meldekortperioder.all { it.tiltakstype == tiltakstype }) { "Alle meldekortperioder må ha samme tiltakstype. Meldekortperioder.tiltakstype=$tiltakstype, meldekortperioders tiltakstyper=${meldekortperioder.map { it.tiltakstype }}" }
    }
}
