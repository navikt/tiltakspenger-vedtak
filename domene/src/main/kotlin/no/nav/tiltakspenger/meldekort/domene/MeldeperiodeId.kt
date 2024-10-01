package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.periodisering.Periode
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Opprettet hovedsakelig for å kunne kjede meldekort (like meldeperioder) på tvers av utbetalinger.
 *
 * En sekundærnøkkel, sammen med sakId/saksnummer for meldekort.
 * Denne er unik for en sak og en meldeperiode.
 * Kan brukes for å identifisere en meldeperiode i en sak, uavhengig om den har blitt korrigert.
 * En [no.nav.tiltakspenger.libs.common.MeldekortId] er unik på tvers av saker og brukes til å identifisere en enkelt meldekorthendelse (utfylling). Uavhengig om det er en førstegangsutfylling eller korrigering.
 */
data class MeldeperiodeId(val verdi: String) {

    val periode: Periode = Periode(
        fraOgMed = LocalDate.parse(verdi.split("/")[0]),
        tilOgMed = LocalDate.parse(verdi.split("/")[1]),
    )
    val fraOgMed: LocalDate = periode.fraOgMed
    val tilOgMed: LocalDate = periode.tilOgMed

    init {
        // Kommentar jah: Foreløpig har vi disse begrensningene. Beroende hva vi blir enig med Dagpenger og AAP, kan vi vurdere om vi skal akseptere meldeperioder på en uke for spesialtilfeller som uke 53.
        require(periode.antallDager == 14L) {
            "En meldeperiode må være 14 dager. Var ${periode.antallDager} dager."
        }
        require(fraOgMed.dayOfWeek == DayOfWeek.MONDAY) { "MeldekortIdForSak må starte på en mandag" }
        require(tilOgMed.dayOfWeek == DayOfWeek.SUNDAY) { "MeldekortIdForSak må slutte på en søndag" }
    }

    override fun toString() = verdi

    companion object {
        fun fraPeriode(periode: Periode): MeldeperiodeId {
            return MeldeperiodeId("${periode.fraOgMed}/${periode.tilOgMed}")
        }
    }
}
