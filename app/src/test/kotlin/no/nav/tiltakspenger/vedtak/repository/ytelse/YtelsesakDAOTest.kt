package no.nav.tiltakspenger.vedtak.repository.ytelse

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.objectmothers.søkerMedTiltak
import no.nav.tiltakspenger.vedtak.objectmothers.tomYtelsesak
import no.nav.tiltakspenger.vedtak.objectmothers.ytelseSak
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
class YtelsesakDAOTest {
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
        val ytelsesakDAO = YtelsesakDAO()
        val søkerRepository = PostgresSøkerRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val søker = søkerMedTiltak(ident = ident)
        søkerRepository.lagre(søker)

        val ytelseSak = tomYtelsesak()

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
    fun `lagre og hente med non-null felter`() {
        val ytelsesakDAO = YtelsesakDAO()
        val søkerRepository = PostgresSøkerRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val søker = søkerMedTiltak(ident = ident)
        søkerRepository.lagre(søker)

        val ytelseSak = ytelseSak()

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
}
