package no.nav.tiltakspenger.vedtak.repository.sak

import io.kotest.matchers.shouldBe
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

            val sak1 = testDataHelper.persisterOpprettetFørstegangsbehandling(
                løpenummer = 1001,
            ).first
            val sak2 = testDataHelper.persisterOpprettetFørstegangsbehandling(
                løpenummer = 1002,
            ).first

            sakRepo.hentForIdent(sak1.fnr) shouldBe Saker(sak1.fnr, listOf(sak1))
            sakRepo.hentForSaksnummer(saksnummer = sak1.saksnummer)!! shouldBe sak1
            sakRepo.hent(sak1.id) shouldBe sak1
            sakRepo.hentSakDetaljer(sak1.id) shouldBe sak1.sakDetaljer
        }
    }

    @Test
    fun `hentForIdent skal hente saker med matchende ident`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val sakRepo = testDataHelper.sakRepo
            val søknadRepo = testDataHelper.søknadRepo

            val ident = random.nextInt().toString()

            val sak1 = testDataHelper.persisterOpprettetFørstegangsbehandling(
                ident = ident,
                løpenummer = 1001,
            ).first
            val sak2 = testDataHelper.persisterOpprettetFørstegangsbehandling(
                ident = ident,
                løpenummer = 1002,
            ).first
            testDataHelper.persisterOpprettetFørstegangsbehandling(
                løpenummer = 1003,
            )

            sakRepo.hentForIdent(ident) shouldBe Saker(ident, listOf(sak1, sak2))
        }
    }
}
