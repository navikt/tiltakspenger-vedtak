package no.nav.tiltakspenger.vedtak.repository.søker

import io.kotest.matchers.collections.shouldContainExactly
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Testcontainers
internal class PostgresSøkerRepositoryTest {
    private val søkerRepo = PostgresSøkerRepository()

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeAll
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre og hente bare søker`() {
        val ident = "1"
        val søker = Søker(ident)

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.hent(ident)!!

        assertEquals(søker.ident, hentetSøker.ident)
        assertEquals(søker.id, hentetSøker.id)
        assertEquals(søker.tilstand, hentetSøker.tilstand)
    }

    @Test
    fun `lagre og hente hele aggregatet`() {
        val ident = "2"

        val søknad1 = Søknad(
            id = UUID.randomUUID(),
            søknadId = "41",
            journalpostId = "42",
            dokumentInfoId = "43",
            fornavn = null,
            etternavn = null,
            ident = ident,
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = null,
            oppholdInstitusjon = null,
            typeInstitusjon = null,
            opprettet = null,
            barnetillegg = emptyList(),
            tidsstempelHosOss = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            tiltak = Tiltak.BrukerregistrertTiltak(
                tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
                arrangoernavn = "Tiltaksarrangør AS",
                beskrivelse = "Beskrivelse",
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now(),
                adresse = "Min adresse",
                postnummer = "0491",
                antallDager = 4
            ),
            trygdOgPensjon = emptyList(),
            fritekst = null,
        )

        val søknad2 = Søknad(
            id = UUID.randomUUID(),
            søknadId = "41",
            journalpostId = "42",
            dokumentInfoId = "43",
            fornavn = null,
            etternavn = null,
            ident = ident,
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = null,
            oppholdInstitusjon = null,
            typeInstitusjon = null,
            opprettet = null,
            barnetillegg = listOf(
                Barnetillegg.MedIdent(
                    alder = 0,
                    ident = "1",
                    land = "NO",
                    fornavn = "fornavn",
                    etternavn = "etternavn",
                )
            ),
            tidsstempelHosOss = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            tiltak = Tiltak.ArenaTiltak(
                arenaId = "123",
                arrangoernavn = "Hurra meg rundt AS",
                harSluttdatoFraArena = true,
                tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
                erIEndreStatus = false,
                opprinneligSluttdato = LocalDate.now(),
                opprinneligStartdato = LocalDate.now(),
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now()
            ),
            trygdOgPensjon = listOf(
                TrygdOgPensjon(
                    utbetaler = "Storebrand", prosent = null, fom = LocalDate.of(2020, 10, 1), tom = null
                )
            ),
            fritekst = null,
        )

        val søknad3 = Søknad(
            id = UUID.randomUUID(),
            søknadId = "41",
            journalpostId = "42",
            dokumentInfoId = "43",
            fornavn = "Johnny",
            etternavn = "McPerson",
            ident = ident,
            deltarKvp = true,
            deltarIntroduksjonsprogrammet = true,
            oppholdInstitusjon = true,
            typeInstitusjon = "Barnevernet",
            opprettet = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            barnetillegg = listOf(
                Barnetillegg.MedIdent(
                    alder = 16,
                    ident = "1",
                    land = "NO",
                    fornavn = "foranvn",
                    etternavn = "etternavn",
                )
            ),
            tidsstempelHosOss = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            tiltak = Tiltak.ArenaTiltak(
                arenaId = "123",
                arrangoernavn = "Tiltaksbedriften AS",
                harSluttdatoFraArena = true,
                tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
                erIEndreStatus = true,
                opprinneligSluttdato = LocalDate.now(),
                opprinneligStartdato = LocalDate.now(),
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now()
            ),
            trygdOgPensjon = listOf(
                TrygdOgPensjon(
                    utbetaler = "Storebrand",
                    prosent = 50,
                    fom = LocalDate.of(2020, 10, 1),
                    tom = LocalDate.of(2020, 10, 1)
                )
            ),
            fritekst = "Fritekst",
        )

        val søker = Søker.fromDb(
            id = UUID.randomUUID(),
            ident = ident,
            tilstand = "AvventerPersonopplysninger",
            søknader = listOf(
                søknad1, søknad2, søknad3
            ),
            personopplysninger = null
        )

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.hent(ident)!!

        assertEquals(søker.ident, hentetSøker.ident)
        assertEquals(søker.id, hentetSøker.id)
        assertEquals(søker.tilstand, hentetSøker.tilstand)
        hentetSøker.søknader shouldContainExactly listOf(søknad1, søknad2, søknad3)
    }
}
