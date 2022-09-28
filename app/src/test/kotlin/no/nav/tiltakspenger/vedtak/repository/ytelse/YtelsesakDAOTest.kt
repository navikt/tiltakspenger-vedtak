package no.nav.tiltakspenger.vedtak.repository.ytelse

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.domene.mars
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
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
        val søker = Søker(ident)
        søkerRepository.lagre(søker)

        val tidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

        val ytelseSak = YtelseSak(
            fomGyldighetsperiode = tidspunkt,
            tomGyldighetsperiode = tidspunkt,
            datoKravMottatt = null,
            dataKravMottatt = null,
            fagsystemSakId = null,
            status = null,
            ytelsestype = null,
            antallDagerIgjen = null,
            antallUkerIgjen = null,
            tidsstempelHosOss = tidspunkt,
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
    fun `lagre og hente med non-null felter`() {
        val ytelsesakDAO = YtelsesakDAO()
        val søkerRepository = PostgresSøkerRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)

        val tidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

        val ytelseSak = YtelseSak(
            fomGyldighetsperiode = tidspunkt,
            tomGyldighetsperiode = tidspunkt,
            datoKravMottatt = 1.mars(2022),
            dataKravMottatt = "Dette er litt data",
            fagsystemSakId = 3,
            status = YtelseSak.YtelseSakStatus.AKTIV,
            ytelsestype = YtelseSak.YtelseSakYtelsetype.DAGP,
            antallDagerIgjen = 7,
            antallUkerIgjen = 1,
            tidsstempelHosOss = tidspunkt,
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
}
