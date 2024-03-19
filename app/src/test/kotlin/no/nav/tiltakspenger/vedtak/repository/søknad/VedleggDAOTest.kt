package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.sessionOf
import no.nav.tiltakspenger.objectmothers.ObjectMother.sakMedOpprettetBehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Vedlegg
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class VedleggDAOTest {

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre vedlegg og hente de ut igjen`() {
        val repository = PostgresSakRepo()
        val sak = sakMedOpprettetBehandling()

        repository.lagre(sak)
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

        val vedleggDAO = VedleggDAO()
        val søknadId = sak.behandlinger.filterIsInstance<Førstegangsbehandling>().first().søknad().id
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                vedleggDAO.lagre(
                    søknadId = søknadId,
                    vedlegg = listOf(vedleggMedNull, vedleggUtenNull),
                    txSession,
                )
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                vedleggDAO.hentVedleggListe(søknadId = søknadId, txSession = txSession)
            }
        }

        assertEquals(2, hentet.size)
        assertTrue(hentet.contains(vedleggMedNull))
        assertTrue(hentet.contains(vedleggUtenNull))
    }
}
