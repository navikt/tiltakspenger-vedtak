package no.nav.tiltakspenger.vedtak.repository.søknad

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.*
import kotlin.reflect.full.declaredMemberProperties

@Testcontainers
internal class SøknadDAOTest {
    private val søknadDAO = SøknadDAO()
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
            tiltak = Tiltak.ArenaTiltak(
                arenaId = "123",
                arrangoernavn = "Tiltaksarrangør AS",
                harSluttdatoFraArena = false,
                tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
                erIEndreStatus = false,
                opprinneligSluttdato = null,
                opprinneligStartdato = LocalDate.now(),
                sluttdato = null,
                startdato = LocalDate.now()
            ),
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
                Barnetillegg.MedIdent(
                    alder = 0,
                    ident = "1",
                    land = "NO",
                    fornavn = "fornavn",
                    etternavn = "etternavn",
                )
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
                startdato = LocalDate.now()
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

        assertNotNull(hentet.first().tiltak)
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
        val tiltak = Tiltak.ArenaTiltak(
            arenaId = "123",
            arrangoernavn = "Tiltaksbedriften AS",
            harSluttdatoFraArena = true,
            tiltakskode = Tiltaksaktivitet.Tiltak.ARBTREN,
            erIEndreStatus = true,
            opprinneligSluttdato = LocalDate.now(),
            opprinneligStartdato = LocalDate.now(),
            sluttdato = LocalDate.now(),
            startdato = LocalDate.now()
        )
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
                Barnetillegg.MedIdent(
                    alder = 16,
                    ident = "1",
                    land = "NO",
                    fornavn = "foranvn",
                    etternavn = "etternavn",
                )
            ),
            tidsstempelHosOss = innhentet,
            tiltak = tiltak,
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

        assertNotNull(hentet.first().tiltak)
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

        søknadHentet.tiltak shouldBe tiltak
        assertEquals(tiltak, søknadHentet.tiltak)

        val trygdOgPensjon = søknadHentet.trygdOgPensjon.first()
        trygdOgPensjon::class.declaredMemberProperties.forEach {
            assertNotNull(it.call(trygdOgPensjon))
        }

        // Sjekker verdiene for noen litt tilfeldige felter også:
        assertEquals(søknad.opprettet, hentet.first().opprettet)
        println(søknad.opprettet)
        assertEquals(søknad.opprettet, hentet.first().opprettet)
        assertEquals(søknad.tidsstempelHosOss, hentet.first().tidsstempelHosOss)
        assertEquals(søknad.deltarKvp, hentet.first().deltarKvp)

        assertEquals(
            (søknad.tiltak as Tiltak.ArenaTiltak).sluttdato,
            (hentet.first().tiltak as Tiltak.ArenaTiltak).sluttdato
        )
        assertEquals(søknad.trygdOgPensjon.first().fom, hentet.first().trygdOgPensjon.first().fom)
    }
}
