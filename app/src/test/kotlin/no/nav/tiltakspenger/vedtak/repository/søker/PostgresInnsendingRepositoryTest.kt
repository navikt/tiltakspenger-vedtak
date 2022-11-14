package no.nav.tiltakspenger.vedtak.repository.søker

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.barnetilleggMedIdent
import no.nav.tiltakspenger.objectmothers.barnetilleggUtenIdent
import no.nav.tiltakspenger.objectmothers.innsendingMedSøknad
import no.nav.tiltakspenger.objectmothers.innsendingMedYtelse
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.objectmothers.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.objectmothers.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.skjermingFalse
import no.nav.tiltakspenger.objectmothers.skjermingTrue
import no.nav.tiltakspenger.objectmothers.tiltaksaktivitet
import no.nav.tiltakspenger.objectmothers.trygdOgPensjon
import no.nav.tiltakspenger.objectmothers.ytelseSak
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
internal class PostgresInnsendingRepositoryTest {
    private val søkerRepo = PostgresInnsendingRepository()

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
        val innsending = Innsending(ident)

        søkerRepo.lagre(innsending)

        val hentetSøker = søkerRepo.hent(ident)!!

        assertEquals(innsending.ident, hentetSøker.ident)
        assertEquals(innsending.id, hentetSøker.id)
        assertEquals(innsending.tilstand, hentetSøker.tilstand)
        innsending.personopplysninger shouldBe emptyList()
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

        val søker = innsendingMedYtelse(
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
        hentetSøker.søknad shouldBe søknad
        hentetSøker.personopplysninger shouldContainExactly listOf(personopplysninger.copy(skjermet = false))
        hentetSøker.tiltak shouldContainExactly tiltaksaktivitet
        hentetSøker.ytelser shouldContainExactly ytelseSak
        hentetSøker.aktivitetslogg shouldBeEqualToComparingFields søker.aktivitetslogg
    }

    @Test
    fun `lagre og hente basert på søknadId`() {
        val ident = Random().nextInt().toString()

        val søknad = nySøknadMedBrukerTiltak(
            ident = ident,
            barnetillegg = listOf(barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )

        val søker = innsendingMedSøknad(
            ident = ident,
            søknad = søknad,
        )

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.findBySøknadId(søknad.søknadId)
        assertNotNull(hentetSøker)
        assertEquals(ident, søker.ident)
        assertEquals(ident, søker.søknad!!.ident)
        assertEquals(ident, hentetSøker!!.ident)
        assertEquals(ident, hentetSøker.søknad!!.ident)
        assertEquals(søker.id, hentetSøker.id)
        assertEquals(søker.tilstand, hentetSøker.tilstand)
        hentetSøker.søknad shouldBe søknad
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

        val søker = innsendingMedYtelse(
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
        hentetSøker.søknad shouldBe søknad
        hentetSøker.personopplysninger shouldBe listOf(personopplysninger.copy(skjermet = true))
        hentetSøker.tiltak shouldContainExactly tiltaksaktivitet
        hentetSøker.ytelser shouldContainExactly ytelseSak
        hentetSøker.aktivitetslogg shouldBeEqualToComparingFields søker.aktivitetslogg
    }
}
