package no.nav.tiltakspenger.vedtak.repository.sak

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test
import java.util.Random

internal class SakRepoTest {
    companion object {
        val random = Random()
    }

    @Test
    fun `lagre og hente en sak med en søknad`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val sakRepo = testDataHelper.sakRepo

            val sak1 =
                testDataHelper
                    .persisterOpprettetFørstegangsbehandling(
                        løpenummer = 1001,
                    ).first
            testDataHelper
                .persisterOpprettetFørstegangsbehandling(
                    løpenummer = 1002,
                ).first

            sakRepo.hentForFnr(sak1.fnr) shouldBe Saker(sak1.fnr, listOf(sak1))
            sakRepo.hentForSaksnummer(saksnummer = sak1.saksnummer)!! shouldBe sak1
            sakRepo.hentForSakId(sak1.id) shouldBe sak1
            sakRepo.hentDetaljerForSakId(sak1.id) shouldBe sak1.sakDetaljer
        }
    }

    @Test
    fun `hentForIdent skal hente saker med matchende ident`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val sakRepo = testDataHelper.sakRepo
            val søknadRepo = testDataHelper.søknadRepo

            val fnr = Fnr.random()

            val sak1 =
                testDataHelper
                    .persisterOpprettetFørstegangsbehandling(
                        fnr = fnr,
                        løpenummer = 1001,
                    ).first
            val sak2 =
                testDataHelper
                    .persisterOpprettetFørstegangsbehandling(
                        fnr = fnr,
                        løpenummer = 1002,
                    ).first
            testDataHelper.persisterOpprettetFørstegangsbehandling(
                løpenummer = 1003,
            )

            sakRepo.hentForFnr(fnr) shouldBe Saker(fnr, listOf(sak1, sak2))
        }
    }
}
