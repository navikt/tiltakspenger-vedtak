package no.nav.tiltakspenger.vedtak.repository.innsending

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.barnetilleggMedIdent
import no.nav.tiltakspenger.objectmothers.barnetilleggUtenIdent
import no.nav.tiltakspenger.objectmothers.innsendingMedSøknad
import no.nav.tiltakspenger.objectmothers.innsendingMedYtelse
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.objectmothers.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.objectmothers.nySøknadMottattHendelse
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
    private val innsendingRepository = PostgresInnsendingRepository()

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeAll
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre og hente bare innsending`() {
        val journalpostId = Random().nextInt().toString()
        val innsending = Innsending(journalpostId)

        innsendingRepository.lagre(innsending)

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!

        assertEquals(innsending.journalpostId, hentetInnsending.journalpostId)
        assertEquals(innsending.id, hentetInnsending.id)
        assertEquals(innsending.tilstand, hentetInnsending.tilstand)
        innsending.personopplysninger shouldBe emptyList()
    }

    @Test
    fun `lagre og oppdatere skal fikse ident`() {
        val journalpostId = Random().nextInt().toString()
        val innsending = Innsending(journalpostId)

        innsendingRepository.lagre(innsending)

        val hendelse = nySøknadMottattHendelse(journalpostId = journalpostId)
        val ident = hendelse.søknad().ident
        innsending.håndter(hendelse)
        innsendingRepository.lagre(innsending)

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!

        assertEquals(innsending.journalpostId, hentetInnsending.journalpostId)
        assertEquals(innsending.id, hentetInnsending.id)
        assertEquals(ident, hentetInnsending.ident)
        assertEquals(innsending.tilstand, hentetInnsending.tilstand)
        innsending.personopplysninger shouldBe emptyList()
    }

    @Test
    fun `lagre og hente hele aggregatet med BrukerTiltak`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()

        val søknad = nySøknadMedBrukerTiltak(
            ident = ident,
            journalpostId = journalpostId,
            barnetillegg = listOf(barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )
        val personopplysninger = personopplysningKjedeligFyr(ident = ident, strengtFortroligUtland = false)
        val tiltaksaktivitet = listOf(tiltaksaktivitet())
        val ytelseSak = listOf(ytelseSak())

        val innsending = innsendingMedYtelse(
            journalpostId = journalpostId,
            ident = ident,
            søknad = søknad,
            personopplysninger = listOf(personopplysninger),
            skjerming = skjermingFalse(ident = ident),
            tiltaksaktivitet = tiltaksaktivitet,
            ytelseSak = ytelseSak,
        )

        innsendingRepository.lagre(innsending)

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!

        assertEquals(innsending.journalpostId, hentetInnsending.journalpostId)
        assertEquals(innsending.ident, hentetInnsending.ident)
        assertEquals(innsending.id, hentetInnsending.id)
        assertEquals(innsending.tilstand, hentetInnsending.tilstand)
        hentetInnsending.søknad shouldBe søknad
        hentetInnsending.personopplysninger shouldContainExactly listOf(personopplysninger.copy(skjermet = false))
        hentetInnsending.tiltak shouldContainExactly tiltaksaktivitet
        hentetInnsending.ytelser shouldContainExactly ytelseSak
        hentetInnsending.aktivitetslogg shouldBeEqualToComparingFields innsending.aktivitetslogg
    }

    @Test
    fun `lagre og hente basert på søknadId`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()

        val søknad = nySøknadMedBrukerTiltak(
            journalpostId = journalpostId,
            ident = ident,
            barnetillegg = listOf(barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )

        val innsending = innsendingMedSøknad(
            journalpostId = journalpostId,
            ident = ident,
            søknad = søknad,
        )

        innsendingRepository.lagre(innsending)

        val hentetInnsending = innsendingRepository.findBySøknadId(søknad.søknadId)
        assertNotNull(hentetInnsending)
        assertEquals(ident, innsending.ident)
        assertEquals(ident, innsending.søknad!!.ident)
        assertEquals(ident, hentetInnsending!!.ident)
        assertEquals(ident, hentetInnsending.søknad!!.ident)
        assertEquals(innsending.id, hentetInnsending.id)
        assertEquals(innsending.tilstand, hentetInnsending.tilstand)
        hentetInnsending.søknad shouldBe søknad
    }

    @Test
    fun `lagre og hente hele aggregatet med ArenaTiltak`() {
        val ident = Random().nextInt().toString()
        val journalpostId = Random().nextInt().toString()

        val søknad = nySøknadMedArenaTiltak(
            journalpostId = journalpostId,
            ident = ident,
            barnetillegg = listOf(barnetilleggUtenIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )
        val personopplysninger = personopplysningKjedeligFyr(ident = ident, strengtFortroligUtland = false)
        val tiltaksaktivitet = listOf(tiltaksaktivitet())
        val ytelseSak = listOf(ytelseSak())

        val innsending = innsendingMedYtelse(
            journalpostId = journalpostId,
            ident = ident,
            søknad = søknad,
            personopplysninger = listOf(personopplysninger),
            skjerming = skjermingTrue(ident = ident),
            tiltaksaktivitet = tiltaksaktivitet,
            ytelseSak = ytelseSak,
        )

        innsendingRepository.lagre(innsending)

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!

        assertEquals(innsending.journalpostId, hentetInnsending.journalpostId)
        assertEquals(innsending.ident, hentetInnsending.ident)
        assertEquals(innsending.id, hentetInnsending.id)
        assertEquals(innsending.tilstand, hentetInnsending.tilstand)
        hentetInnsending.søknad shouldBe søknad
        hentetInnsending.personopplysninger shouldBe listOf(personopplysninger.copy(skjermet = true))
        hentetInnsending.tiltak shouldContainExactly tiltaksaktivitet
        hentetInnsending.ytelser shouldContainExactly ytelseSak
        hentetInnsending.aktivitetslogg shouldBeEqualToComparingFields innsending.aktivitetslogg
    }
}
