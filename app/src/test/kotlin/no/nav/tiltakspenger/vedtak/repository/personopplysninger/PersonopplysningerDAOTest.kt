package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

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

    @Disabled("Venter på implementasjon av PersonopplysningerDAO")
    @Test
    fun `lagre og hent`() {
        val dao = PersonopplysningerDAO()
        val ident = "42"
        val personopplysninger = Personopplysninger(
            ident = ident,
            fødselsdato = LocalDate.of(1970, Month.JANUARY, 1),
            fornavn = "Kjell",
            mellomnavn = "T.",
            etternavn = "Ring",
            fortrolig = false,
            strengtFortrolig = true,
            kommune = "Oslo",
            bydel = "Bjerke",
            land = "Norge",
            skjermet = null,
            innhentet = LocalDateTime.now()
        )

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(personopplysninger, txSession)
            }
        }
        val hentet = sessionOf(DataSource.hikariDataSource).use {
          it.transaction { txSession ->
              dao.hent(ident, txSession)
          }
        }

        assertEquals(personopplysninger, hentet)

    }

}
