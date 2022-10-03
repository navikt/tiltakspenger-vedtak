package no.nav.tiltakspenger.vedtak.repository.søker

import io.kotest.matchers.collections.shouldContainExactly
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.objectmothers.barnetilleggMedIdent
import no.nav.tiltakspenger.vedtak.objectmothers.barnetilleggUtenIdent
import no.nav.tiltakspenger.vedtak.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.vedtak.objectmothers.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.vedtak.objectmothers.tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.objectmothers.trygdOgPensjon
import no.nav.tiltakspenger.vedtak.objectmothers.ytelseSak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
internal class PostgresSøkerRepositoryTest {
    private val søkerRepo = PostgresSøkerRepository()

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeAll
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre og hente bare søker`() {
        val ident = "1"
        val søker = Søker(ident)

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.hent(ident)!!

        assertEquals(søker.ident, hentetSøker.ident)
        assertEquals(søker.id, hentetSøker.id)
        assertEquals(søker.tilstand, hentetSøker.tilstand)
    }

    @Test
    fun `lagre og hente hele aggregatet`() {
        val ident = Random().nextInt().toString()

        val søknad1 = nySøknadMedBrukerTiltak(
            ident = ident,
        )

        val søknad2 = nySøknadMedArenaTiltak(
            ident = ident,
            barnetillegg = listOf(barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon())
        )

        val søknad3 = nySøknadMedBrukerTiltak(
            ident = ident,
            barnetillegg = listOf(barnetilleggUtenIdent(), barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon())
        )

        val tiltaksaktivitet = tiltaksaktivitet()
        val ytelseSak = ytelseSak()

        val søker = Søker.fromDb(
            id = UUID.randomUUID(),
            ident = ident,
            tilstand = "AvventerPersonopplysninger",
            søknader = listOf(
                søknad1, søknad2, søknad3
            ),
            tiltak = listOf(tiltaksaktivitet),
            ytelser = listOf(ytelseSak),
            personopplysninger = null,
        )

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.hent(ident)!!

        assertEquals(søker.ident, hentetSøker.ident)
        assertEquals(søker.id, hentetSøker.id)
        assertEquals(søker.tilstand, hentetSøker.tilstand)
        hentetSøker.søknader shouldContainExactly listOf(søknad1, søknad2, søknad3)
        hentetSøker.tiltak shouldContainExactly listOf(tiltaksaktivitet)
        hentetSøker.ytelser shouldContainExactly listOf(ytelseSak)
    }
}
