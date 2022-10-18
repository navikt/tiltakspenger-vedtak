package no.nav.tiltakspenger.vedtak.repository.søker

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.barnetilleggMedIdent
import no.nav.tiltakspenger.objectmothers.barnetilleggUtenIdent
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.objectmothers.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.objectmothers.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.skjermingFalse
import no.nav.tiltakspenger.objectmothers.skjermingTrue
import no.nav.tiltakspenger.objectmothers.søkerMedYtelse
import no.nav.tiltakspenger.objectmothers.tiltaksaktivitet
import no.nav.tiltakspenger.objectmothers.trygdOgPensjon
import no.nav.tiltakspenger.objectmothers.ytelseSak
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
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
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.hent(ident)!!

        assertEquals(søker.ident, hentetSøker.ident)
        assertEquals(søker.id, hentetSøker.id)
        assertEquals(søker.tilstand, hentetSøker.tilstand)
        søker.personopplysninger shouldBe emptyList()
    }

    @Test
    fun `lagre og hente hele aggregatet med BrukerTiltak`() {
        val ident = Random().nextInt().toString()

        val søknad = nySøknadMedBrukerTiltak(
            ident = ident,
            barnetillegg = listOf(barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )
        val personopplysninger = personopplysningKjedeligFyr(ident = ident, strengtFortroligUtland = false)
        val tiltaksaktivitet = listOf(tiltaksaktivitet())
        val ytelseSak = listOf(ytelseSak())

        val søker = søkerMedYtelse(
            ident = ident,
            søknad = søknad,
            personopplysninger = listOf(personopplysninger),
            skjerming = skjermingFalse(ident = ident),
            tiltaksaktivitet = tiltaksaktivitet,
            ytelseSak = ytelseSak,
        )

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.hent(ident)!!

        assertEquals(søker.ident, hentetSøker.ident)
        assertEquals(søker.id, hentetSøker.id)
        assertEquals(søker.tilstand, hentetSøker.tilstand)
        hentetSøker.søknader shouldContainExactly listOf(søknad)
        hentetSøker.personopplysninger shouldContainExactly listOf(personopplysninger.copy(skjermet = false))
        hentetSøker.tiltak shouldContainExactly tiltaksaktivitet
        hentetSøker.ytelser shouldContainExactly ytelseSak
        hentetSøker.aktivitetslogg shouldBeEqualToComparingFields søker.aktivitetslogg
    }

    @Test
    fun `lagre og hente hele aggregatet med ArenaTiltak`() {
        val ident = Random().nextInt().toString()

        val søknad = nySøknadMedArenaTiltak(
            ident = ident,
            barnetillegg = listOf(barnetilleggUtenIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )
        val personopplysninger = personopplysningKjedeligFyr(ident = ident, strengtFortroligUtland = false)
        val tiltaksaktivitet = listOf(tiltaksaktivitet())
        val ytelseSak = listOf(ytelseSak())

        val søker = søkerMedYtelse(
            ident = ident,
            søknad = søknad,
            personopplysninger = listOf(personopplysninger),
            skjerming = skjermingTrue(ident = ident),
            tiltaksaktivitet = tiltaksaktivitet,
            ytelseSak = ytelseSak,
        )

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.hent(ident)!!

        assertEquals(søker.ident, hentetSøker.ident)
        assertEquals(søker.id, hentetSøker.id)
        assertEquals(søker.tilstand, hentetSøker.tilstand)
        hentetSøker.søknader shouldContainExactly listOf(søknad)
        hentetSøker.personopplysninger shouldBe listOf(personopplysninger.copy(skjermet = true))
        hentetSøker.tiltak shouldContainExactly tiltaksaktivitet
        hentetSøker.ytelser shouldContainExactly ytelseSak
        hentetSøker.aktivitetslogg shouldBeEqualToComparingFields søker.aktivitetslogg
    }
}
