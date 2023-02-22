package no.nav.tiltakspenger.vedtak.repository.overgangsstønad

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedForeldrepenger
import no.nav.tiltakspenger.objectmothers.ObjectMother.overgangsstønadVedtak
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random

@Testcontainers
class OvergangsstønadDAOTest {
    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre og hente med null felter`() {
        val overgangsstønadVedtakDAO = OvergangsstønadVedtakDAO()
        val repository = PostgresInnsendingRepository(overgangsstønadVedtakDAO = overgangsstønadVedtakDAO)
        val ident = Random().nextInt().toString()
        val innsending = innsendingMedForeldrepenger(ident = ident)
        repository.lagre(innsending)

        val overgangsstønadVedtak = listOf(overgangsstønadVedtak())

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                overgangsstønadVedtakDAO.lagre(innsending.id, overgangsstønadVedtak, txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                overgangsstønadVedtakDAO.hentForInnsending(innsendingId = innsending.id, txSession = txSession)
            }
        }

        hentet shouldNotBe null
        hentet.first() shouldBe overgangsstønadVedtak.first().copy(
            id = hentet.first().id,
        )
    }
}
