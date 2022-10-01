package no.nav.tiltakspenger.vedtak.repository.ytelse

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.domene.mars
import no.nav.tiltakspenger.vedtak.testcommon.nyYtelsesak
import no.nav.tiltakspenger.vedtak.testcommon.søkerRegistrert
import no.nav.tiltakspenger.vedtak.testcommon.ytelsevedtakGodkjent
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import no.nav.tiltakspenger.vedtak.testcommon.søkerMedSøknad
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
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
    fun `lagre og hente med null felter`() {
        val ytelsevedtakDAO = YtelsevedtakDAO()
        val ytelsesakDAO = YtelsesakDAO()
        val søkerRepository = PostgresSøkerRepository(ytelsesakDAO = ytelsesakDAO)
        val ident = Random().nextInt().toString()
        val søker = søkerMedSøknad(ident = ident)

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
    fun `lagre og hente med vedtak`() {
        val ytelsesakDAO = YtelsesakDAO()
        val søkerRepository = PostgresSøkerRepository(ytelsesakDAO = ytelsesakDAO)
        val søker = søkerRegistrert()
        søkerRepository.lagre(søker)

        val vedtak = ytelsevedtakGodkjent()
        val ytelsesak = nyYtelsesak(vedtak = listOf(vedtak))

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.lagre(søker.id, listOf(ytelsesak), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                ytelsesakDAO.hentForSøker(søkerId = søker.id, txSession = txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first().vedtak.first() shouldBe ytelsesak.vedtak.first()
    }
}
