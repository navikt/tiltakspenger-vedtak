package no.nav.tiltakspenger.vedtak.repository.behandling

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.behandling.Tiltak
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.Saksnummer
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random

@Testcontainers
internal class TiltakDAOTest {

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
        val tiltakDAO = TiltakDAO()

        val tiltak = Tiltak(
            id = "123",
            gjennomføring = Tiltak.Gjennomføring(
                id = "",
                arrangørnavn = "arrangør",
                typeNavn = "Jobbkurs",
                typeKode = "JOBBK",
                rettPåTiltakspenger = true,
                fom = null,
                tom = null,
            ),
            deltakelseFom = 1.januar(2023),
            deltakelseTom = 31.januar(2023),
            deltakelseStatus = Tiltak.DeltakerStatus(status = "DELTAR", rettTilÅASøke = true),
            deltakelseDagerUke = null,
            deltakelseProsent = null,
            kilde = "Komet",
            registrertDato = 1.januarDateTime(2023),
            innhentet = 1.januarDateTime(2023),
        )

        val behandling = lagreSakOgBehandling()
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                tiltakDAO.lagre(behandling.id, listOf(tiltak), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                tiltakDAO.hent(behandling.id, txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe tiltak
    }

    @Test
    fun `lagre og hente med non-null felter`() {
        val tiltakDAO = TiltakDAO()
        val tiltak = Tiltak(
            id = "123",
            gjennomføring = Tiltak.Gjennomføring(
                id = "123",
                arrangørnavn = "arrangør",
                typeNavn = "Jobbkurs",
                typeKode = "JOBBK",
                rettPåTiltakspenger = true,
                fom = 1.januar(2023),
                tom = 31.mars(2023),
            ),
            deltakelseFom = 1.januar(2023),
            deltakelseTom = 31.mars(2023),
            deltakelseStatus = Tiltak.DeltakerStatus(status = "DELTAR", rettTilÅASøke = true),
            deltakelseDagerUke = 2.0F,
            deltakelseProsent = 100.0F,
            kilde = "Komet",
            registrertDato = 1.januarDateTime(2023),
            innhentet = 1.januarDateTime(2023),
        )

        val behandling = lagreSakOgBehandling()

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                tiltakDAO.lagre(behandling.id, listOf(tiltak), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                tiltakDAO.hent(behandling.id, txSession)
            }
        }

        hentet.size shouldBe 1
        hentet.first() shouldBe tiltak
    }

    private fun lagreSakOgBehandling(): Søknadsbehandling {
        val behandlingRepo = PostgresBehandlingRepo()
        val sakRepo = PostgresSakRepo()

        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val deltakelseFom = 1.januar(2023)
        val deltakelseTom = 31.mars(2023)
        val sakId = SakId.random()
        val sak = Sak(
            id = sakId,
            ident = ident,
            saknummer = Saksnummer(verdi = "123"),
            periode = Periode(fra = deltakelseFom, til = deltakelseTom),
            behandlinger = listOf(),
            personopplysninger = listOf(),
        )
        sakRepo.lagre(sak)

        val søknad = ObjectMother.nySøknad(
            journalpostId = journalpostId,
            personopplysninger = ObjectMother.personSøknad(
                ident = ident,
            ),
            tiltak = ObjectMother.søknadTiltak(
                deltakelseFom = deltakelseFom,
                deltakelseTom = deltakelseTom,
            ),
            barnetillegg = listOf(ObjectMother.barnetilleggMedIdent()),
        )

        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(sakId = sakId, søknad = søknad)

        return behandlingRepo.lagre(behandling)
    }
}
