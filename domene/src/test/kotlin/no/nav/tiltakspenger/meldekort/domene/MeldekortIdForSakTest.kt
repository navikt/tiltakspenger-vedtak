package no.nav.tiltakspenger.meldekort.domene

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import org.junit.jupiter.api.Test

internal class MeldekortIdForSakTest {

    @Test
    fun fraPeriode() {
        val periode = Periode(1.mars(2021), 14.mars(2021))
        val meldekortIdForSak = MeldekortIdForSak.fraPeriode(periode)
        "2021-03-01" shouldBe meldekortIdForSak.fraOgMed.toString()
        "2021-03-14" shouldBe meldekortIdForSak.tilOgMed.toString()
        meldekortIdForSak.verdi shouldBe "2021-03-01/2021-03-14"
    }
}
