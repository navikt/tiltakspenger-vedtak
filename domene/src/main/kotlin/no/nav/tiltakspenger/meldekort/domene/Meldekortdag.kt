package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.IkkeTiltaksdag
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Tiltaksdag
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsdag
import java.time.LocalDate

/**
 * Når en bruker er på tiltak kan hen være 1-5 av 7 dager i uken på tiltak.
 * Når bruker har registrert sine maks antall dager på meldekortet [Tiltaksdag], vil de resterende dagene være [IkkeTiltaksdag].
 * [IkkeTiltaksdag] blir også omtalt som sperret.
 * Selvom en dag er en [Tiltaksdag], trenger ikke bruker å ha deltatt, men hen må ha meldt dagen.
 */
sealed interface Meldekortdag {
    val dato: LocalDate
    val meldekortId: MeldekortId

    /**
     * En dag bruker har vært på tiltak.
     */
    data class Tiltaksdag(
        override val dato: LocalDate,
        // TODO jah denne PR: Vi må legge til ingen utbetaling igjen. Evt. kan vi ha en egen klasse for det.
        val status: Utbetalingsdag.Status,
        override val meldekortId: MeldekortId,
        val tiltakstype: TiltakstypeSomGirRett,
    ) : Meldekortdag

    /**
     * En dag bruker ikke har vært på tiltak. Blir også omtalt som sperret.
     */
    data class IkkeTiltaksdag(
        override val dato: LocalDate,
        override val meldekortId: MeldekortId,
    ) : Meldekortdag
}
