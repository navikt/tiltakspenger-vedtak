package no.nav.tiltakspenger.vedtak.repository.ytelse

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.objectmothers.søkerMedTiltak
import no.nav.tiltakspenger.vedtak.objectmothers.søkerMedYtelse
import no.nav.tiltakspenger.vedtak.objectmothers.tomYtelsevedtak
import no.nav.tiltakspenger.vedtak.objectmothers.ytelseSak
import no.nav.tiltakspenger.vedtak.objectmothers.ytelseVedtak
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
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
    fun `lagre hele søker med ytelser og vedtak med null verdier og hente den ut igjen`() {
        val ytelsesakDAO = YtelsesakDAO()
        val søkerRepository = PostgresSøkerRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val søker = søkerMedYtelse(
            ident = ident,
            ytelseSak = listOf(ytelseSak(vedtak = listOf(tomYtelsevedtak())))
        )

        søkerRepository.lagre(søker)

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.hentForSøker(søkerId = søker.id, txSession = txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe søker.ytelser.first()
    }

    @Test
    fun `lagre søker med ytelser og vedtak med null verdier og hente den ut igjen`() {
        val ytelsesakDAO = YtelsesakDAO()
        val søkerRepository = PostgresSøkerRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val søker = søkerMedTiltak(ident = ident)
        søkerRepository.lagre(søker)

        val ytelseSak = ytelseSak(
            vedtak = listOf(tomYtelsevedtak())
        )

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.lagre(søker.id, listOf(ytelseSak), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.hentForSøker(søkerId = søker.id, txSession = txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe ytelseSak
    }

    @Test
    fun `lagre søker med ytelser og vedtak med verdier og hente den ut igjen`() {
        val ytelsesakDAO = YtelsesakDAO()
        val søkerRepository = PostgresSøkerRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val søker = søkerMedYtelse(
            ident = ident,
            ytelseSak = listOf(ytelseSak(vedtak = listOf(ytelseVedtak())))
        )

        søkerRepository.lagre(søker)

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.hentForSøker(søkerId = søker.id, txSession = txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe søker.ytelser.first()
    }
}
