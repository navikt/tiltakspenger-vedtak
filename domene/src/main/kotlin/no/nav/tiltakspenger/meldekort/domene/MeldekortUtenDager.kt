package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.periodisering.Periode

/**
 * Kun en oversikt over hvilke meldekort
 * TODO denne-pr: I frontend viser vi meldekortperiodene på venstre siden i menyen, de er klikkbare, men vi kan uansett bare fylle ut den første.
 *   Nå genererer vi bare et ikkeutfylt meldekort om gangen, så dette gir ikke like mye mening lenger.
 */
data class MeldekortUtenDager(
    val id: MeldekortId,
    val periode: Periode,
    val status: MeldekortStatus,
) {
    enum class MeldekortStatus {
        UTFYLT,
        IKKE_UTFYLT,
    }
}
