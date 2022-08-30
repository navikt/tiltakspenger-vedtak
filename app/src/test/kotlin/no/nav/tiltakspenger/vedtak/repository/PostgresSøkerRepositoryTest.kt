package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource.session
import no.nav.tiltakspenger.vedtak.db.TestPostgreSQLContainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
internal class PostgresSøkerRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgreSQLContainer = TestPostgreSQLContainer.instance
    }

    @BeforeAll
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre og hente`() {
        val ident = "1"
        val søker = Søker(ident)

        val antallRader = PostgresSøkerRepository.lagre(søker)

        assertEquals(1, antallRader)

        val hentetSøker = PostgresSøkerRepository.hent(ident)

        assertEquals(søker.ident(), hentetSøker?.ident())
        assertEquals(søker.id(), hentetSøker?.id())
//        assertEquals(søker.tilstand(), hentetSøker?.tilstand())
    }

    @Test
    fun `lagre og hente med eksplisitt session`() {
        val ident = "2"
        val søker = Søker(ident)

        val antallRader = PostgresSøkerRepository.lagre(søker)

        assertEquals(1, antallRader)

        val hentetSøker = PostgresSøkerRepository.hentSøker(ident, session)

        assertEquals(søker.ident(), hentetSøker?.ident())
        assertEquals(søker.id(), hentetSøker?.id())
    }
}
