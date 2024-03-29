package no.nav.tiltakspenger.vedtak.repository.ytelse

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedSkjerming
import no.nav.tiltakspenger.objectmothers.ObjectMother.tomYtelsesak
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseSak
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseVedtak
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
        val repository = PostgresInnsendingRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val innsending = innsendingMedSkjerming(ident = ident)
        repository.lagre(innsending)

        val ytelseSak = tomYtelsesak()

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
    fun `lagre og hente med non-null felter`() {
        val ytelsesakDAO = YtelsesakDAO()
        val repository = PostgresInnsendingRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val innsending = innsendingMedSkjerming(ident = ident)
        repository.lagre(innsending)

        val ytelseSak = ytelseSak()

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
    fun `lagre to ganger med vedtak og hente`() {
        /*
        Denne testen er innført for å avdekke og regresjonsteste følgende feil:
        ERROR: update or delete on table "ytelsesak" violates foreign key constraint
        "ytelsevedtak_ytelsesak_id_fkey" on table "ytelsevedtak"
        Detail: Key (id)=(9d6ac352-5f55-411b-a091-8315bbaf06a6) is still referenced from table "ytelsevedtak".
         */

        val ytelsesakDAO = YtelsesakDAO()
        val repository = PostgresInnsendingRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val innsending = innsendingMedSkjerming(ident = ident)
        repository.lagre(innsending)

        val ytelseSak = ytelseSak(vedtak = listOf(ytelseVedtak()))

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.lagre(innsending.id, listOf(ytelseSak), txSession)
            }
        }

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
}
