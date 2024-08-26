package no.nav.tiltakspenger.vedtak.repository.meldekort

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterIverksattFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test

class MeldekortRepoImplTest {
    @Test
    fun `kan lagre og hente`() {
        withMigratedDb {
            val testDataHelper = TestDataHelper(it)
            val sak = testDataHelper.persisterIverksattFørstegangsbehandling()
            val meldekort =
                ObjectMother.meldekort(
                    sakId = sak.id,
                    rammevedtakId = sak.vedtak.single().id,
                )
            val meldekortRepo = testDataHelper.meldekortRepo
            meldekortRepo.lagre(meldekort)
            meldekortRepo.hentForMeldekortId(meldekort.id)!! shouldBe meldekort
        }
    }
}
