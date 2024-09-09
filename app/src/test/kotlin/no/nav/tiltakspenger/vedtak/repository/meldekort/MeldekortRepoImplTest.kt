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
                ObjectMother.utfyltMeldekort(
                    sakId = sak.id,
                    rammevedtakId = sak.vedtak.single().id,
                    fnr = sak.fnr,
                )
            val meldekortRepo = testDataHelper.meldekortRepo
            meldekortRepo.lagre(meldekort)
            val hentForMeldekortId = meldekortRepo.hentForMeldekortId(meldekort.id)!!
            hentForMeldekortId shouldBe meldekort
        }
    }
}
