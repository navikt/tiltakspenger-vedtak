package no.nav.tiltakspenger.vedtak.repository.søknad

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.august
import no.nav.tiltakspenger.objectmothers.ObjectMother.kvpJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.kvpNei
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeNei
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.Vedlegg
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.temporal.ChronoUnit
import java.util.Random
import kotlin.reflect.full.declaredMemberProperties

@Testcontainers
internal class SøknadDAOTest {
    private val søknadDAO = SøknadDAO()
    private val repository = PostgresInnsendingRepository(søknadDAO)

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test

    @Test
    fun `lagre og hente med null-felter`() {
        val ident = "3"
        val journalpostId = Random().nextInt().toString()
        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
        repository.lagre(innsending)
        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
        val uuid = Søknad.randomId()
        val søknad = Søknad(
            id = uuid,
            søknadId = "41",
            journalpostId = "42",
            dokumentInfoId = "43",
            personopplysninger = Søknad.Personopplysninger(
                fornavn = "fornavn",
                etternavn = "etternavn",
                ident = ident,
            ),
            kvp = kvpNei(),
            intro = periodeNei(),
            institusjon = null,
            innsendt = null,
            barnetillegg = emptyList(),
            tidsstempelHosOss = innhentet,
            tiltak = Tiltak.ArenaTiltak(
                arenaId = "123",
                arrangoernavn = "Tiltaksarrangør AS",
                tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
                opprinneligSluttdato = null,
                opprinneligStartdato = LocalDate.now(),
                sluttdato = null,
                startdato = LocalDate.now(),
            ),
            trygdOgPensjon = emptyList(),
            fritekst = null,
            vedlegg = emptyList(),
        )
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(innsending.id, søknad, txSession)
            }
        }

        val hentet: Søknad? = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.hent(innsending.id, txSession)
            }
        }

        assertNotNull(hentet)
        assertEquals(uuid, hentet!!.id)
        assertEquals(ident, hentet.personopplysninger.ident)
        assertEquals(innhentet, hentet.tidsstempelHosOss)
    }

    @Test
    fun `lagre og hente med null-felter og underliggende klasser`() {
        val ident = "4"
        val journalpostId = Random().nextInt().toString()
        val innsending = Innsending(journalpostId = journalpostId, ident = ident)

        repository.lagre(innsending)
        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
        val uuid = Søknad.randomId()
        val søknad = Søknad(
            id = uuid,
            søknadId = "41",
            journalpostId = "42",
            dokumentInfoId = "43",
            personopplysninger = Søknad.Personopplysninger(
                fornavn = "fornavn",
                etternavn = "etternavn",
                ident = ident,
            ),
            kvp = kvpNei(),
            intro = periodeNei(),
            institusjon = null,
            innsendt = null,
            barnetillegg = listOf(
                Barnetillegg.FraPdl(
                    oppholderSegIEØS = "NO",
                    fornavn = "fornavn",
                    mellomnavn = "mellomnavn",
                    etternavn = "etternavn",
                    fødselsdato = "1",
                ),
            ),
            tidsstempelHosOss = innhentet,
            tiltak = Tiltak.ArenaTiltak(
                arenaId = "123",
                arrangoernavn = "Hurra meg rundt AS",
                harSluttdatoFraArena = true,
                tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
                erIEndreStatus = false,
                opprinneligSluttdato = LocalDate.now(),
                opprinneligStartdato = LocalDate.now(),
                sluttdato = null,
                startdato = LocalDate.now(),
            ),
            trygdOgPensjon = listOf(
                TrygdOgPensjon(
                    utbetaler = "Storebrand",
                    prosent = null,
                    fom = LocalDate.of(2020, 10, 1),
                    tom = null,
                ),
            ),
            fritekst = null,
            vedlegg = listOf(
                Vedlegg(
                    journalpostId = "journalpostId",
                    dokumentInfoId = "dokumentId",
                    filnavn = "filnavn",
                ),
            ),
        )
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(innsending.id, søknad, txSession)
            }
        }

        val hentet: Søknad? = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.hent(innsending.id, txSession)
            }
        }

        assertNotNull(hentet)
        assertEquals(uuid, hentet!!.id)
        assertEquals(ident, hentet.personopplysninger.ident)
        assertEquals(innhentet, hentet.tidsstempelHosOss)

        assertNotNull(hentet.tiltak)
        assertEquals(1, hentet.barnetillegg.size)
        assertEquals(1, hentet.trygdOgPensjon.size)
        assertEquals(1, hentet.vedlegg.size)
        assertEquals(false, hentet.intro.deltar)
        assertNull(hentet.intro.periode)
        assertEquals(false, hentet.kvp.deltar)
        assertNull(hentet.kvp.periode)
    }

    @Test
    fun `lagre og hente med fyllte felter og underliggende klasser`() {
        val ident = "5"
        val journalpostId = Random().nextInt().toString()
        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
        repository.lagre(innsending)
        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
        val uuid = Søknad.randomId()
        val tiltak = Tiltak.ArenaTiltak(
            arenaId = "123",
            arrangoernavn = "Tiltaksbedriften AS",
            harSluttdatoFraArena = true,
            tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
            erIEndreStatus = true,
            opprinneligSluttdato = LocalDate.now(),
            opprinneligStartdato = LocalDate.now(),
            sluttdato = LocalDate.now(),
            startdato = LocalDate.now(),
        )
        val søknad = Søknad(
            id = uuid,
            søknadId = "41",
            journalpostId = "42",
            dokumentInfoId = "43",
            personopplysninger = Søknad.Personopplysninger(
                fornavn = "fornavn",
                etternavn = "etternavn",
                ident = ident,
            ),
            kvp = kvpJa(
                fra = 15.august(2022),
                til = 30.august(2022),
            ),
            intro = periodeJa(
                fra = 15.august(2022),
                til = 30.august(2022),
            ),
            institusjon = true,
            innsendt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            barnetillegg = listOf(
                Barnetillegg.FraPdl(
                    oppholderSegIEØS = "NO",
                    fornavn = "foranvn",
                    mellomnavn = "mellomnavn",
                    etternavn = "etternavn",
                    fødselsdato = "1",
                ),
            ),
            tidsstempelHosOss = innhentet,
            tiltak = tiltak,
            trygdOgPensjon = listOf(
                TrygdOgPensjon(
                    utbetaler = "Storebrand",
                    prosent = 50,
                    fom = LocalDate.of(2020, 10, 1),
                    tom = LocalDate.of(2020, 10, 1),
                ),
            ),
            fritekst = "Fritekst",
            vedlegg = listOf(
                Vedlegg(
                    journalpostId = "journalpostId",
                    dokumentInfoId = "dokumentId",
                    filnavn = "filnavn",
                ),
            ),
        )
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(innsending.id, søknad, txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.hent(innsending.id, txSession)
            }
        }

        assertNotNull(hentet)
        assertEquals(uuid, hentet!!.id)
        assertEquals(ident, hentet.personopplysninger.ident)
        assertEquals(innhentet, hentet.tidsstempelHosOss)

        assertNotNull(hentet.tiltak)
        assertEquals(1, hentet.barnetillegg.size)
        assertEquals(1, hentet.trygdOgPensjon.size)

        hentet::class.declaredMemberProperties.forEach {
            assertNotNull(it.call(hentet))
        }

        assertEquals(søknad.intro, hentet.intro)
        assertEquals(søknad.kvp, hentet.kvp)

        val barnetillegg = hentet.barnetillegg.first()
        barnetillegg::class.declaredMemberProperties.forEach {
            assertNotNull(it.call(barnetillegg))
        }

        hentet.tiltak shouldBe tiltak
        assertEquals(tiltak, hentet.tiltak)

        val trygdOgPensjon = hentet.trygdOgPensjon.first()
        trygdOgPensjon::class.declaredMemberProperties.forEach {
            assertNotNull(it.call(trygdOgPensjon))
        }

        // Sjekker verdiene for noen litt tilfeldige felter også:
        assertEquals(søknad.innsendt, hentet.innsendt)
        println(søknad.innsendt)
        assertEquals(søknad.innsendt, hentet.innsendt)
        assertEquals(søknad.tidsstempelHosOss, hentet.tidsstempelHosOss)

        assertEquals(
            (søknad.tiltak as Tiltak.ArenaTiltak).sluttdato,
            (hentet.tiltak as Tiltak.ArenaTiltak).sluttdato,
        )
        assertEquals(søknad.trygdOgPensjon.first().fom, hentet.trygdOgPensjon.first().fom)

        assertEquals(søknad.vedlegg.first().journalpostId, hentet.vedlegg.first().journalpostId)
        assertEquals(søknad.vedlegg.first().dokumentInfoId, hentet.vedlegg.first().dokumentInfoId)
        assertEquals(søknad.vedlegg.first().filnavn, hentet.vedlegg.first().filnavn)
    }
}
