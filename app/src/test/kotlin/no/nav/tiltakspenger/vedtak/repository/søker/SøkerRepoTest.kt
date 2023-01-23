package no.nav.tiltakspenger.vedtak.repository.søker

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.nySøker
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random

@Testcontainers
internal class SøkerRepoTest {

    companion object {
        @Container
        val testcontainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    private val repo = SøkerRepository()

    @Test
    fun `lagre og hent`() {
        val ident = Random().nextInt().toString()
        val søker = nySøker(ident = ident)
        repo.lagre(søker)

        val hentet = repo.hent(søker.søkerId)

        hentet?.ident shouldBe søker.ident
        hentet?.personopplysninger shouldBe søker.personopplysninger
        hentet?.søkerId shouldBe søker.søkerId
    }
}
