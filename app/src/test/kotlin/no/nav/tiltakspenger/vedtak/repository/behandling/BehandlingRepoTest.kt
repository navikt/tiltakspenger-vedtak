package no.nav.tiltakspenger.vedtak.repository.behandling

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.objectmothers.ObjectMother.sakMedOpprettetBehandling
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test
import java.util.Random

internal class BehandlingRepoTest {

    companion object {
        val random = Random()
    }

    @Test
    fun `lagre og hente en behandling`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val behandlingRepo = testDataHelper.behandlingRepo
            val sakRepo = testDataHelper.sakRepo

            val (sak, _) = testDataHelper.persisterOpprettetFørstegangsbehandling()
            sakRepo.hent(sak.id) shouldBe sak
            behandlingRepo.hent(sak.førstegangsbehandling.id) shouldBe sak.førstegangsbehandling
        }
    }

    @Test
    fun `hentAlleForIdent skal kun hente behandlinger for en ident og ikke de andre`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val behandlingRepo = testDataHelper.behandlingRepo
            val sakRepo = testDataHelper.sakRepo

            val ident = random.nextInt().toString()
            val vårSakId = SakId.random()
            val enAnnenSakId = SakId.random()
            val sakForVårIdent = sakMedOpprettetBehandling(sakId = vårSakId, ident = ident)
            val enAnnenSak = sakMedOpprettetBehandling(sakId = enAnnenSakId, ident = "random", løpenummer = 1002)

            sakRepo.lagre(sakForVårIdent)
            sakRepo.lagre(enAnnenSak)

            val hentBehandling = behandlingRepo.hentAlleForIdent(ident)

            hentBehandling.size shouldBe 1
        }
    }
}
