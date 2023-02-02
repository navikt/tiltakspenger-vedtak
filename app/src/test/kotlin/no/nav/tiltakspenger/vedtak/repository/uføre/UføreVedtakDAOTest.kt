package no.nav.tiltakspenger.vedtak.repository.uføre

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedForeldrepenger
import no.nav.tiltakspenger.objectmothers.ObjectMother.uføreVedtak
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
class UføreVedtakDAOTest {
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
        val uføreVedtakDAO = UføreVedtakDAO()
        val repository = PostgresInnsendingRepository(uføreVedtakDAO = uføreVedtakDAO)
        val ident = Random().nextInt().toString()
        val innsending = innsendingMedForeldrepenger(ident = ident)
        repository.lagre(innsending)

        val uføreVedtak = uføreVedtak()

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                uføreVedtakDAO.lagre(innsending.id, uføreVedtak, txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                uføreVedtakDAO.hentForInnsending(innsendingId = innsending.id, txSession = txSession)
            }
        }

        hentet shouldNotBe null
        hentet shouldBe uføreVedtak.copy(
            id = hentet!!.id,
        )
    }
}
