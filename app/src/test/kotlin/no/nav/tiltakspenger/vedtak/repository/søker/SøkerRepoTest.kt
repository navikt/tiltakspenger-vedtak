package no.nav.tiltakspenger.vedtak.repository.søker

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøker
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test
import java.util.Random

internal class SøkerRepoTest {

    companion object {
        val random = Random()
    }

    @Test
    fun `lagre og hent`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val repo = testDataHelper.søkerRepo

            val fnr = Fnr.random()
            val søker = nySøker(fnr = fnr)
            repo.lagre(søker)

            val hentet = repo.hent(søker.søkerId)

            hentet?.fnr shouldBe søker.fnr
            hentet?.personopplysninger shouldBe søker.personopplysninger
            hentet?.søkerId shouldBe søker.søkerId
        }
    }
}
