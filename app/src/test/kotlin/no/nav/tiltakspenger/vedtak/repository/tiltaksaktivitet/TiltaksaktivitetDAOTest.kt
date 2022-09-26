package no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
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
    fun `lagre arenatiltak og hente den ut igjen`() {
        val tiltaksaktivitetDAO = TiltaksaktivitetDAO()
        val søkerRepository = PostgresSøkerRepository(tiltaksaktivitetDAO = tiltaksaktivitetDAO)
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)

        val tiltaksaktivitet = Tiltaksaktivitet(
            tiltak = Tiltaksaktivitet.Tiltak.JOBBK,
            aktivitetId = "aktid",
            tiltakLokaltNavn = null,
            arrangoer = null,
            bedriftsnummer = null,
            deltakelsePeriode = null,
            deltakelseProsent = null,
            deltakerStatus = Tiltaksaktivitet.DeltakerStatus.AKTUELL,
            statusSistEndret = null,
            begrunnelseInnsoeking = "begrunnelse",
            antallDagerPerUke = null,
            innhentet = LocalDateTime.now(),
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

        hentet.first().tiltak shouldBe Tiltaksaktivitet.Tiltak.JOBBK
    }
}
