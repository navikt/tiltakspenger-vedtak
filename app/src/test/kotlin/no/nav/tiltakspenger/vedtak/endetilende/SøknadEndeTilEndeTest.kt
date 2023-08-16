package no.nav.tiltakspenger.vedtak.endetilende

import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class SøknadEndeTilEndeTest {
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
}
