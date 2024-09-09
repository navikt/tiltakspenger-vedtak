package no.nav.tiltakspenger.vedtak.repository.søknad

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Vedlegg
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterSøknad
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test

internal class VedleggDAOTest {
    @Test
    fun `lagre vedlegg og hente de ut igjen`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val søknad = testDataHelper.persisterSøknad()
            val søknadId = søknad.id
            val vedleggMedNull =
                Vedlegg(
                    journalpostId = "journalpostId",
                    dokumentInfoId = "dokumentInfoId",
                    filnavn = null,
                )
            val vedleggUtenNull =
                Vedlegg(
                    journalpostId = "journalpostId",
                    dokumentInfoId = "dokumentInfoId",
                    filnavn = "filnavn",
                )

            testDataHelper.sessionFactory.withTransaction { txSession ->
                VedleggDAO.lagre(
                    søknadId = søknadId,
                    vedlegg = listOf(vedleggMedNull, vedleggUtenNull),
                    txSession,
                )
            }

            val hentet =
                testDataHelper.sessionFactory.withTransaction { txSession ->
                    VedleggDAO.hentVedleggListe(søknadId = søknadId, session = txSession)
                }

            hentet.size shouldBe 2
            hentet.contains(vedleggMedNull) shouldBe true
            hentet.contains(vedleggUtenNull) shouldBe true
        }
    }
}
