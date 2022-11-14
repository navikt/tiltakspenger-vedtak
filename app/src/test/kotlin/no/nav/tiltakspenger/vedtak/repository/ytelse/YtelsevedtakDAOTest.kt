package no.nav.tiltakspenger.vedtak.repository.ytelse

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.objectmothers.innsendingMedTiltak
import no.nav.tiltakspenger.objectmothers.innsendingMedYtelse
import no.nav.tiltakspenger.objectmothers.tomYtelsevedtak
import no.nav.tiltakspenger.objectmothers.ytelseSak
import no.nav.tiltakspenger.objectmothers.ytelseVedtak
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
class YtelsevedtakDAOTest {
    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre hele innsending med ytelser og vedtak med null verdier og hente den ut igjen`() {
        val ytelsesakDAO = YtelsesakDAO()
        val repository = PostgresInnsendingRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val innsending = innsendingMedYtelse(
            ident = ident,
            ytelseSak = listOf(ytelseSak(vedtak = listOf(tomYtelsevedtak())))
        )

        repository.lagre(innsending)

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.hentForInnsending(innsendingId = innsending.id, txSession = txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe innsending.ytelser.first()
    }

    @Test
    fun `lagre innsending med ytelser og vedtak med null verdier og hente den ut igjen`() {
        val ytelsesakDAO = YtelsesakDAO()
        val repository = PostgresInnsendingRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val innsending = innsendingMedTiltak(ident = ident)
        repository.lagre(innsending)

        val ytelseSak = ytelseSak(
            vedtak = listOf(tomYtelsevedtak())
        )

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.lagre(innsending.id, listOf(ytelseSak), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.hentForInnsending(innsendingId = innsending.id, txSession = txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe ytelseSak
    }

    @Test
    fun `lagre innsending med ytelser og vedtak med verdier og hente den ut igjen`() {
        val ytelsesakDAO = YtelsesakDAO()
        val repository = PostgresInnsendingRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val innsending = innsendingMedYtelse(
            ident = ident,
            ytelseSak = listOf(ytelseSak(vedtak = listOf(ytelseVedtak())))
        )

        repository.lagre(innsending)

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.hentForInnsending(innsendingId = innsending.id, txSession = txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe innsending.ytelser.first()
    }
}
