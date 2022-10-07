package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import io.kotest.matchers.collections.shouldContainExactly
import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.objectmothers.barn
import no.nav.tiltakspenger.vedtak.objectmothers.personopplysningKjedeligFyr
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
internal class PersonopplysningerDAOTest {

    companion object {
        @Container
        val testcontainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    private val dao = PersonopplysningerDAO()
    private val søkerRepository = PostgresSøkerRepository()
    private fun personopplysninger(ident: String) = Personopplysninger.Søker(
        ident = ident,
        fødselsdato = LocalDate.of(1970, Month.JANUARY, 1),
        fornavn = "Kjell",
        mellomnavn = "T.",
        etternavn = "Ring",
        fortrolig = false,
        strengtFortrolig = true,
        skjermet = true,
        kommune = "Oslo",
        bydel = "3440",
        tidsstempelHosOss = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    )

    @Test
    fun `lagre og hent`() {
        // given
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val personopplysninger = personopplysninger(ident)

        // when
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, listOf(personopplysninger), txSession)
            }
        }
        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hentPersonopplysningerForSøker(søker.id, txSession)
            }
        }

        // then
        assertEquals(personopplysninger, hentet)
    }

    @Test
    fun `lagre og hent med null-verdier`() {
        // given
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val personopplysninger = Personopplysninger.Søker(
            ident = ident,
            fødselsdato = LocalDate.of(1970, Month.JANUARY, 1),
            fornavn = "Kjell",
            mellomnavn = null,
            etternavn = "Ring",
            fortrolig = false,
            strengtFortrolig = true,
            skjermet = null,
            kommune = null,
            bydel = null,
            tidsstempelHosOss = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        )

        // when
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, listOf(personopplysninger), txSession)
            }
        }
        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hentPersonopplysningerForSøker(søker.id, txSession)
            }
        }

        // then
        assertEquals(personopplysninger, hentet)
    }

    @Test
    fun `hent en som ikke finnes skal gi null tilbake`() {
        // when
        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hentPersonopplysningerForSøker(UUID.randomUUID(), txSession)
            }
        }

        // then
        assertNull(hentet)
    }

    @Test
    fun `legg til personopplysninger for en ident som finnes fra før - da skal de nye dataene gjelde`() {
        // given
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val gamlePersonopplysninger = personopplysninger(ident)

        // when
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, listOf(gamlePersonopplysninger), txSession)
            }
        }

        val nyePersonopplysninger = gamlePersonopplysninger.copy(fornavn = "Ole")

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, listOf(nyePersonopplysninger), txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hentPersonopplysningerForSøker(søker.id, txSession)
            }
        }

        // then
        assertEquals(nyePersonopplysninger, hentet)
    }

    @Test
    fun `lagre barn og hent opp igjen`() {
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        val barn1 = barn()
        val barn2 = barn()
        val personopplysninger = personopplysningKjedeligFyr(ident = ident)

        søkerRepository.lagre(søker)

        val personopplysningListe = listOf(personopplysninger) // + barn1 og barn2
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, personopplysningListe, txSession)
            }
        }

        val hentetPersonopplysninger = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hentPersonopplysningerForSøker(søker.id, txSession)
            }
        }

        assertEquals(personopplysninger, hentetPersonopplysninger)

        val hentetPersonopplysningerForBarn: List<Personopplysninger> = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hentPersonopplysningerForBarnMedIdent(søker.id, txSession)
            }
        }

        hentetPersonopplysningerForBarn shouldContainExactly listOf(barn1, barn2)
    }
}
