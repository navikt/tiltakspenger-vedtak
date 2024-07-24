package no.nav.tiltakspenger.vedtak.repository.søknad

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.ObjectMother.sakMedOpprettetBehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Vedlegg
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test

internal class VedleggDAOTest {

    @Test
    fun `lagre vedlegg og hente de ut igjen`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val sakRepo = testDataHelper.sakRepo

            val sak = sakMedOpprettetBehandling()
            val søknadId = sak.behandlinger.filterIsInstance<Førstegangsbehandling>().first().søknad().id
            sakRepo.lagre(sak)

            val vedleggMedNull = Vedlegg(
                journalpostId = "journalpostId",
                dokumentInfoId = "dokumentInfoId",
                filnavn = null,
            )
            val vedleggUtenNull = Vedlegg(
                journalpostId = "journalpostId",
                dokumentInfoId = "dokumentInfoId",
                filnavn = "filnavn",
            )

            testDataHelper.sessionFactory.withTransaction { txSession ->
                testDataHelper.vedleggDAO.lagre(
                    søknadId = søknadId,
                    vedlegg = listOf(vedleggMedNull, vedleggUtenNull),
                    txSession,
                )
            }

            val hentet = testDataHelper.sessionFactory.withTransaction { txSession ->
                testDataHelper.vedleggDAO.hentVedleggListe(søknadId = søknadId, txSession = txSession)
            }

            hentet.size shouldBe 2
            hentet.contains(vedleggMedNull) shouldBe true
            hentet.contains(vedleggUtenNull) shouldBe true
        }
    }
}
