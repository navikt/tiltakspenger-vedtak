package no.nav.tiltakspenger.meldekort.domene

import arrow.core.NonEmptyList
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * saksbehandler/beslutter Dette er fremdeles til avklaring, men inntil videre aksepterer vi at saksbehandler og beslutter er like.
 * @property tiltakstype I MVP støtter vi kun ett tiltak, men på sikt kan vi ikke garantere at det er én til én mellom meldekortperiode og tiltakstype.
 */
data class Meldekortperiode(
    val verdi: NonEmptyList<Meldekortdag>,
) : List<Meldekortdag> by verdi {
    val fraOgMed: LocalDate = verdi.first().dato
    val tilOgMed: LocalDate = verdi.last().dato
    val periode: Periode = Periode(fraOgMed, tilOgMed)

    val tiltakstype: TiltakstypeSomGirRett = verdi.filterIsInstance<Meldekortdag.Tiltaksdag>().first().tiltakstype

    val meldekortId = verdi.first().meldekortId

    init {
        require(verdi.size == 14) { "En meldekortperiode må være 14 dager, men var ${verdi.size}" }
        require(verdi.map { it.dato }.toSet().size == verdi.size) { "Datoer må være unike" }
        require(verdi.first().dato.dayOfWeek == DayOfWeek.MONDAY) { "Utbetalingsperioden må starte på en mandag" }
        require(verdi.last().dato.dayOfWeek == DayOfWeek.SUNDAY) { "Utbetalingsperioden må slutte på en sønad" }
        verdi.forEachIndexed { index, dag ->
            require(verdi.first().dato.plusDays(index.toLong()) == dag.dato) {
                "Datoene må være sammenhengende og sortert, men var ${verdi.map { it.dato }}"
            }
        }
        require(verdi.filterIsInstance<Meldekortdag.Tiltaksdag>().size <= 5 * 2) {
            // TODO jah: Dette bør være en mer felles konstant. Kan også skrive en mer spesifikk sjekk per uke.
            "Det kan maks være 5*2 tiltaksdager i en meldekortperiode, men var ${verdi.filterIsInstance<Meldekortdag.Tiltaksdag>().size}"
        }
        require(verdi.filterIsInstance<Meldekortdag.Tiltaksdag>().all { it.tiltakstype == tiltakstype }) {
            "Alle tiltaksdager må ha samme tiltakstype"
        }
        require(verdi.all { it.meldekortId == meldekortId }) { "Alle dager må tilhøre samme meldekort, men var: ${verdi.map { it.meldekortId }}" }
    }
}
