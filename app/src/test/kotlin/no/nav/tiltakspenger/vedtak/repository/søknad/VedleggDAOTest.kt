package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.sessionOf
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Vedlegg
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

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
        val søknadDAO = SøknadDAO()
        val repository = PostgresInnsendingRepository(søknadDAO)
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
        repository.lagre(innsending)
        val søknadId = Søknad.randomId()
        val søknad = nySøknadMedArenaTiltak(
            id = søknadId,
            ident = ident,
        )
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(innsending.id, søknad, txSession)
            }
        }
        val vedleggMedNull = Vedlegg(
            journalpostId = "journalpostId",
            dokumentInfoId = "dokumentInfoId",
            filnavn = null
        )
        val vedleggUtenNull = Vedlegg(
            journalpostId = "journalpostId",
            dokumentInfoId = "dokumentInfoId",
            filnavn = "filnavn"
        )

        val vedleggDAO = VedleggDAO()
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                vedleggDAO.lagre(
                    søknadId = søknadId,
                    vedlegg = listOf(vedleggMedNull, vedleggUtenNull),
                    txSession
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
