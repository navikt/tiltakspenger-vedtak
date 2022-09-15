package no.nav.tiltakspenger.vedtak.repository.søknad

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.*
import kotlin.reflect.full.declaredMemberProperties
import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.ArenaTiltak
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.BrukerregistrertTiltak
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class PostgresSøknadDAOTest {
    private val søknadDAO = PostgresSøknadDAO()
    private val søkerRepository = PostgresSøkerRepository(søknadDAO)

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre og hente med null-felter`() {
        val ident = "3"
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
        val uuid = UUID.randomUUID()
        val søknad = Søknad(
            id = uuid,
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
            tidsstempelHosOss = innhentet,
            arenaTiltak = null,
            brukerregistrertTiltak = null,
            trygdOgPensjon = emptyList(),
            fritekst = null,
        )
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(søker.id, listOf(søknad), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.hentAlle(søker.id, txSession)
            }
        }

        assertEquals(1, hentet.size)
        assertEquals(uuid, hentet.first().id)
        assertEquals(ident, hentet.first().ident)
        assertEquals(innhentet, hentet.first().tidsstempelHosOss)
    }

    @Test
    fun `lagre og hente med null-felter og underliggende klasser`() {
        val ident = "4"
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
        val uuid = UUID.randomUUID()
        val søknad = Søknad(
            id = uuid,
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
                Barnetillegg(
                    fornavn = null, etternavn = null, alder = 0, ident = "1", land = "NO"
                )
            ),
            tidsstempelHosOss = innhentet,
            arenaTiltak = ArenaTiltak(
                arenaId = null,
                arrangoer = null,
                harSluttdatoFraArena = null,
                tiltakskode = null,
                erIEndreStatus = null,
                opprinneligSluttdato = null,
                opprinneligStartdato = null,
                sluttdato = null,
                startdato = null
            ),
            brukerregistrertTiltak = BrukerregistrertTiltak(
                tiltakskode = null,
                arrangoernavn = null,
                beskrivelse = null,
                fom = null,
                tom = null,
                adresse = null,
                postnummer = null,
                antallDager = 0
            ),
            trygdOgPensjon = listOf(
                TrygdOgPensjon(
                    utbetaler = "Storebrand", prosent = null, fom = LocalDate.of(2020, 10, 1), tom = null
                )
            ),
            fritekst = null,
        )
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(søker.id, listOf(søknad), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.hentAlle(søker.id, txSession)
            }
        }

        assertEquals(1, hentet.size)
        assertEquals(uuid, hentet.first().id)
        assertEquals(ident, hentet.first().ident)
        assertEquals(innhentet, hentet.first().tidsstempelHosOss)

        assertNotNull(hentet.first().arenaTiltak)
        assertNotNull(hentet.first().brukerregistrertTiltak)
        assertEquals(1, hentet.first().barnetillegg.size)
        assertEquals(1, hentet.first().trygdOgPensjon.size)
    }

    @Test
    fun `lagre og hente med fyllte felter og underliggende klasser`() {
        val ident = "5"
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
        val uuid = UUID.randomUUID()
        val søknad = Søknad(
            id = uuid,
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
            opprettet = LocalDateTime.now(),
            barnetillegg = listOf(
                Barnetillegg(
                    fornavn = "Roger", etternavn = "McPerson", alder = 16, ident = "1", land = "NO"
                )
            ),
            tidsstempelHosOss = innhentet,
            arenaTiltak = ArenaTiltak(
                arenaId = "123",
                arrangoer = "Tiltaksbedriften AS",
                harSluttdatoFraArena = true,
                tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
                erIEndreStatus = true,
                opprinneligSluttdato = LocalDate.now(),
                opprinneligStartdato = LocalDate.now(),
                sluttdato = LocalDate.now(),
                startdato = LocalDate.now()
            ),
            brukerregistrertTiltak = BrukerregistrertTiltak(
                tiltakskode = Tiltaksaktivitet.Tiltak.BIO,
                arrangoernavn = "Tiltaksbedriften AS",
                beskrivelse = "Foo bar",
                fom = LocalDate.now(),
                tom = LocalDate.now(),
                adresse = "Osloveien -1",
                postnummer = "0491",
                antallDager = 10
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
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(søker.id, listOf(søknad), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.hentAlle(søker.id, txSession)
            }
        }

        assertEquals(1, hentet.size)
        assertEquals(uuid, hentet.first().id)
        assertEquals(ident, hentet.first().ident)
        assertEquals(innhentet, hentet.first().tidsstempelHosOss)

        assertNotNull(hentet.first().arenaTiltak)
        assertNotNull(hentet.first().brukerregistrertTiltak)
        assertEquals(1, hentet.first().barnetillegg.size)
        assertEquals(1, hentet.first().trygdOgPensjon.size)

        val søknadHentet: Søknad = hentet.first()
        søknadHentet::class.declaredMemberProperties.forEach {
            assertNotNull(it.call(søknadHentet))
        }

        val barnetillegg = søknadHentet.barnetillegg.first()
        barnetillegg::class.declaredMemberProperties.forEach {
            assertNotNull(it.call(barnetillegg))
        }

        val arenaTiltak = søknadHentet.arenaTiltak!!
        arenaTiltak::class.declaredMemberProperties.forEach {
            assertNotNull(it.call(arenaTiltak))
        }

        val brukerregistrertTiltak = søknadHentet.brukerregistrertTiltak!!
        brukerregistrertTiltak::class.declaredMemberProperties.forEach {
            assertNotNull(it.call(brukerregistrertTiltak))
        }

        val trygdOgPensjon = søknadHentet.trygdOgPensjon.first()
        trygdOgPensjon::class.declaredMemberProperties.forEach {
            assertNotNull(it.call(trygdOgPensjon))
        }

        // Sjekker verdiene for noen litt tilfeldige felter også:
        assertEquals(søknad.opprettet, hentet.first().opprettet)
        assertEquals(søknad.tidsstempelHosOss, hentet.first().tidsstempelHosOss)
        assertEquals(søknad.deltarKvp, hentet.first().deltarKvp)

        assertEquals(søknad.arenaTiltak!!.sluttdato, hentet.first().arenaTiltak!!.sluttdato)
        assertEquals(søknad.trygdOgPensjon.first().fom, hentet.first().trygdOgPensjon.first().fom)
    }
}
