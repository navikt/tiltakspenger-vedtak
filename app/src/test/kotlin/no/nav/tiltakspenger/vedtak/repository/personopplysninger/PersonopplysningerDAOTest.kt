package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
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

    @Test
    fun `lagre og hent`() {
        val søkerRepository = PostgresSøkerRepository()
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val dao = PersonopplysningerDAO()
        val personopplysninger = Personopplysninger(
            ident = ident,
            fødselsdato = LocalDate.of(1970, Month.JANUARY, 1),
            fornavn = "Kjell",
            mellomnavn = "T.",
            etternavn = "Ring",
            fortrolig = false,
            strengtFortrolig = true,
            skjermet = null,
            kommune = "Oslo",
            bydel = "Bjerke",
            land = "Norge",
            tidsstempelHosOss = LocalDateTime.now()
        )

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, personopplysninger, txSession)
            }
        }
        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(søker.id, txSession)
            }
        }

        assertEquals(personopplysninger, hentet)

    }

}
