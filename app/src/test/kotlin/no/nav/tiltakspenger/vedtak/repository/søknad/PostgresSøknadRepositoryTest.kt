package no.nav.tiltakspenger.vedtak.repository.søknad

import java.time.LocalDateTime
import java.time.Month
import java.util.*
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@Disabled("Usikker på hvordan man skal teste denne i isolasjon. En søknad må ha en referanse til Søker når den lagres.")
internal class PostgresSøknadRepositoryTest {
    private val søknadRepository = PostgresSøknadRepository()

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
            barnetillegg = listOf(),
            innhentet = innhentet,
            arenaTiltak = null,
            brukerregistrertTiltak = null,
            trygdOgPensjon = null,
            fritekst = null,
        )
        val antallLagret = søknadRepository.lagre(ident, listOf(søknad))

        assertEquals(1, antallLagret)

        val hentet = søknadRepository.hentAlle(ident)

        assertEquals(1, hentet.size)
        assertEquals(uuid, hentet.first().id)
        assertEquals(ident, hentet.first().ident)
        assertEquals(innhentet, hentet.first().innhentet)
    }
}
