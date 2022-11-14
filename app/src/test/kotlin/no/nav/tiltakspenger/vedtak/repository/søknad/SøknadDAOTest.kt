package no.nav.tiltakspenger.vedtak.repository.søknad

import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.IntroduksjonsprogrammetDetaljer
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.TypeInstitusjon
import no.nav.tiltakspenger.vedtak.Vedlegg
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresInnsendingRepository
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
import kotlin.reflect.full.declaredMemberProperties

@Testcontainers
internal class SøknadDAOTest {
    private val søknadDAO = SøknadDAO()
    private val søkerRepository = PostgresInnsendingRepository(søknadDAO)

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
        val innsending = Innsending(ident)
        søkerRepository.lagre(innsending)
        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
        val uuid = Søknad.randomId()
        val søknad = Søknad(
            id = uuid,
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
            vedlegg = emptyList(),
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

        assertEquals(1, hentet.size)
        assertEquals(uuid, hentet.first().id)
        assertEquals(ident, hentet.first().ident)
        assertEquals(innhentet, hentet.first().tidsstempelHosOss)
    }

    @Test
    fun `lagre og hente med null-felter og underliggende klasser`() {
        val ident = "4"
        val innsending = Innsending(ident)
        søkerRepository.lagre(innsending)
        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
        val uuid = Søknad.randomId()
        val søknad = Søknad(
            id = uuid,
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
            barnetillegg = listOf(
                Barnetillegg.MedIdent(
                    alder = 0,
                    ident = "1",
                    oppholdsland = "NO",
                    fornavn = "fornavn",
                    mellomnavn = "mellomnavn",
                    etternavn = "etternavn",
                    søktBarnetillegg = true,
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
            vedlegg = listOf(
                Vedlegg(
                    journalpostId = "journalpostId",
                    dokumentInfoId = "dokumentId",
                    filnavn = "filnavn",
                )
            )
        )
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(innsending.id, listOf(søknad), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.hent(innsending.id, txSession)
            }
        }

        assertEquals(1, hentet.size)
        assertEquals(uuid, hentet.first().id)
        assertEquals(ident, hentet.first().ident)
        assertEquals(innhentet, hentet.first().tidsstempelHosOss)

        assertNotNull(hentet.first().tiltak)
        assertEquals(1, hentet.first().barnetillegg.size)
        assertEquals(1, hentet.first().trygdOgPensjon.size)
        assertEquals(1, hentet.first().vedlegg.size)
        assertNull(hentet.first().introduksjonsprogrammetDetaljer)
    }

    @Test
    fun `lagre og hente med fyllte felter og underliggende klasser`() {
        val ident = "5"
        val innsending = Innsending(ident)
        søkerRepository.lagre(innsending)
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
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljer(
                fom = LocalDate.of(2022, Month.AUGUST, 15),
                tom = LocalDate.of(2022, Month.AUGUST, 30)
            ),
            oppholdInstitusjon = true,
            typeInstitusjon = TypeInstitusjon.BARNEVERN,
            opprettet = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            barnetillegg = listOf(
                Barnetillegg.MedIdent(
                    alder = 16,
                    ident = "1",
                    oppholdsland = "NO",
                    fornavn = "foranvn",
                    mellomnavn = "mellomnavn",
                    etternavn = "etternavn",
                    søktBarnetillegg = true,
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
            vedlegg = listOf(
                Vedlegg(
                    journalpostId = "journalpostId",
                    dokumentInfoId = "dokumentId",
                    filnavn = "filnavn",
                )
            )
        )
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(innsending.id, listOf(søknad), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.hent(innsending.id, txSession)
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

        assertEquals(søknad.introduksjonsprogrammetDetaljer, søknadHentet.introduksjonsprogrammetDetaljer)

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

        assertEquals(søknad.vedlegg.first().journalpostId, hentet.first().vedlegg.first().journalpostId)
        assertEquals(søknad.vedlegg.first().dokumentInfoId, hentet.first().vedlegg.first().dokumentInfoId)
        assertEquals(søknad.vedlegg.first().filnavn, hentet.first().vedlegg.first().filnavn)
    }
}
