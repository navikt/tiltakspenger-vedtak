package no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Testcontainers
internal class TiltaksaktivitetDAOTest {

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
        val tiltaksaktivitetDAO = TiltaksaktivitetDAO()
        val søkerRepository = PostgresSøkerRepository(tiltaksaktivitetDAO = tiltaksaktivitetDAO)
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)

        val tidspunkt = LocalDateTime.now()

        val tiltaksaktivitet = Tiltaksaktivitet(
            tiltak = Tiltaksaktivitet.Tiltak.JOBBK,
            aktivitetId = "aktid",
            tiltakLokaltNavn = null,
            arrangør = null,
            bedriftsnummer = null,
            deltakelsePeriode = Tiltaksaktivitet.DeltakelsesPeriode(null, null),
            deltakelseProsent = null,
            deltakerStatus = Tiltaksaktivitet.DeltakerStatus.AKTUELL,
            statusSistEndret = null,
            begrunnelseInnsøking = null,
            antallDagerPerUke = null,
            tidsstempelHosOss = tidspunkt,
        )

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                tiltaksaktivitetDAO.lagre(søker.id, listOf(tiltaksaktivitet), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                tiltaksaktivitetDAO.hentForSøker(søkerId = søker.id, txSession = txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe tiltaksaktivitet
    }

    @Test
    fun `lagre og hente med non-null felter`() {
        val tiltaksaktivitetDAO = TiltaksaktivitetDAO()
        val søkerRepository = PostgresSøkerRepository(tiltaksaktivitetDAO = tiltaksaktivitetDAO)
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)

        val tidspunkt = LocalDateTime.now()

        val tiltaksaktivitet = Tiltaksaktivitet(
            tiltak = Tiltaksaktivitet.Tiltak.JOBBK,
            aktivitetId = "aktid",
            tiltakLokaltNavn = "lokalt navn",
            arrangør = "Arrangør",
            bedriftsnummer = "bedriftsnummer",
            deltakelsePeriode = Tiltaksaktivitet.DeltakelsesPeriode(
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(1)
            ),
            deltakelseProsent = 50.0F,
            deltakerStatus = Tiltaksaktivitet.DeltakerStatus.AKTUELL,
            statusSistEndret = LocalDate.now(),
            begrunnelseInnsøking = "begrunnelse",
            antallDagerPerUke = 3.5F,
            tidsstempelHosOss = tidspunkt,
        )

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                tiltaksaktivitetDAO.lagre(søker.id, listOf(tiltaksaktivitet), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                tiltaksaktivitetDAO.hentForSøker(søkerId = søker.id, txSession = txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe tiltaksaktivitet
    }
}
