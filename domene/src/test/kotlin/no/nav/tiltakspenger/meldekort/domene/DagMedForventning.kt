package no.nav.tiltakspenger.meldekort.domene

import arrow.core.NonEmptyList
import arrow.core.flatten
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando.Status
import no.nav.tiltakspenger.objectmothers.ObjectMother
import java.time.LocalDate

internal data class DagMedForventning(
    val dag: LocalDate,
    val status: Status,
    val forventning: ReduksjonAvYtelsePåGrunnAvFravær,
)

internal fun NonEmptyList<NonEmptyList<DagMedForventning>>.assertForventning() {
    val actual = ObjectMother.beregnMeldekortperioder(
        perioder = this.map { outer -> outer.map { SendMeldekortTilBeslutterKommando.Dager.Dag(it.dag, it.status) } },
    )
    actual.utfylteDager.forEachIndexed { index, it ->
        (it.dato to it.reduksjon) shouldBe (this.flatten()[index].dag to flatten()[index].forventning)
    }
}
