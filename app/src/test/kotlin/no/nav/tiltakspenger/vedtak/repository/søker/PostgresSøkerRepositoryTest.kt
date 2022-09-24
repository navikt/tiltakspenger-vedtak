package no.nav.tiltakspenger.vedtak.repository.søker

import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søknad.PostgresSøknadDAO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class PostgresSøkerRepositoryTest {
    private val søknadRepo = PostgresSøknadDAO()
    private val søkerRepo = PostgresSøkerRepository(søknadRepo)

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeAll
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre og hente`() {
        val ident = "1"
        val søker = Søker(ident)

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.hent(ident)

        assertEquals(søker.ident, hentetSøker?.ident)
        assertEquals(søker.id, hentetSøker?.id)
        assertEquals(søker.tilstand, hentetSøker?.tilstand)
    }
}
