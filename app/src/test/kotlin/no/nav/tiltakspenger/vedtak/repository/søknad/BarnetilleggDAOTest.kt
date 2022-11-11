package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.temporal.ChronoUnit
import java.util.*

@Testcontainers
internal class BarnetilleggDAOTest {

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre barnetillegg og hente de ut igjen (må dessverre lagre både søker og søknad pga foreign keys)`() {
        val søknadDAO = SøknadDAO()
        val søkerRepository = PostgresSøkerRepository(søknadDAO)
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val søknadId = Søknad.randomId()
        val søknad = enSøknad(søknadId, ident)
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(søker.id, listOf(søknad), txSession)
            }
        }
        val barnetilleggMedIdent =
            Barnetillegg.MedIdent(
                alder = 42,
                oppholdsland = "SWE",
                ident = "123",
                fornavn = "fornavn",
                mellomnavn = "mellomnavn",
                etternavn = "etternavn",
                søktBarnetillegg = true,
            )
        val barnetilleggUtenIdent =
            Barnetillegg.UtenIdent(
                alder = 42,
                oppholdsland = "SWE",
                fødselsdato = LocalDate.of(2022, Month.AUGUST, 19),
                fornavn = "fornavn",
                mellomnavn = null,
                etternavn = "etternavn",
                søktBarnetillegg = true,
            )

        val barnetilleggDAO = BarnetilleggDAO()
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                barnetilleggDAO.lagre(
                    søknadId = søknadId,
                    barnetillegg = listOf(barnetilleggMedIdent, barnetilleggUtenIdent),
                    txSession
                )
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                barnetilleggDAO.hentBarnetilleggListe(søknadId = søknadId, txSession = txSession)
            }
        }

        assertEquals(2, hentet.size)
        assertTrue(hentet.contains(barnetilleggMedIdent))
        assertTrue(hentet.contains(barnetilleggUtenIdent))
    }

    private fun enSøknad(id: SøknadId, ident: String) = Søknad(
        id = id,
        søknadId = "41",
        journalpostId = "42",
        dokumentInfoId = "43",
        fornavn = null,
        etternavn = null,
        ident = ident,
        deltarKvp = false,
        deltarIntroduksjonsprogrammet = false,
        introduksjonsprogrammetDetaljer = null,
        oppholdInstitusjon = null,
        typeInstitusjon = null,
        opprettet = null,
        barnetillegg = emptyList(),
        tidsstempelHosOss = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        tiltak = Tiltak.ArenaTiltak(
            arenaId = "123",
            arrangoernavn = "Fest og morro",
            harSluttdatoFraArena = true,
            tiltakskode = Tiltaksaktivitet.Tiltak.GRUPPEAMO,
            erIEndreStatus = false,
            opprinneligSluttdato = LocalDate.now(),
            opprinneligStartdato = LocalDate.now(),
            sluttdato = LocalDate.now(),
            startdato = LocalDate.now()
        ),
        trygdOgPensjon = emptyList(),
        fritekst = null,
        vedlegg = emptyList(),
    )
}
