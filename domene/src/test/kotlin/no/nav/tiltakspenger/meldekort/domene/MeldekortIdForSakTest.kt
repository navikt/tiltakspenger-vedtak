package no.nav.tiltakspenger.meldekort.domene

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import org.junit.jupiter.api.Test

internal class MeldekortIdForSakTest {

    @Test
    fun fraPeriode() {
        val periode = Periode(1.mars(2021), 14.mars(2021))
        val meldeperiodeId = MeldeperiodeId.fraPeriode(periode)
        "2021-03-01" shouldBe meldeperiodeId.fraOgMed.toString()
        "2021-03-14" shouldBe meldeperiodeId.tilOgMed.toString()
        meldeperiodeId.verdi shouldBe "2021-03-01/2021-03-14"
    }
}
