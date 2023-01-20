package no.nav.tiltakspenger.vedtak.repository.innsending

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import no.nav.tiltakspenger.vedtak.db.flywayCleanAndMigrate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random

@Testcontainers
internal class PostgresInnsendingRepositoryTest {
    private val innsendingRepository = PostgresInnsendingRepository()

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayCleanAndMigrate()
    }

    @Test
    fun `skal telle antall innsendinger korrekt`() {

        // Flyway legger inn 6
        innsendingRepository.antall() shouldBe 6

        innsendingRepository.lagre(
            Innsending(
                journalpostId = Random().nextInt().toString(),
                ident = Random().nextInt().toString()
            )
        )
        innsendingRepository.antall() shouldBe 7

        innsendingRepository.lagre(
            Innsending(
                journalpostId = Random().nextInt().toString(),
                ident = Random().nextInt().toString()
            )
        )
        innsendingRepository.lagre(
            Innsending(
                journalpostId = Random().nextInt().toString(),
                ident = Random().nextInt().toString()
            )
        )
        innsendingRepository.lagre(
            Innsending(
                journalpostId = Random().nextInt().toString(),
                ident = Random().nextInt().toString()
            )
        )
        innsendingRepository.antall() shouldBe 10
    }

    @Test
    fun `skal telle antall innsendinger hvor faktainnhenting har feilet korrekt`() {

        // Flyway legger inn 6, ingen har feilet.
        // Dette er mao en litt slapp test,
        // men sjekker iallefall om spørringen feiler eller ikke..
        innsendingRepository.antallMedTilstandFaktainnhentingFeilet() shouldBe 0
    }

    @Test
    fun `skal telle antall innsendinger hvor behandlingen ikke er ferdig`() {

        // Flyway legger inn 6, alle er ferdig.
        // Dette er mao en litt slapp test,
        // men sjekker iallefall om spørringen feiler eller ikke..
        innsendingRepository.antallStoppetUnderBehandling() shouldBe 0

        innsendingRepository.lagre(
            Innsending(
                journalpostId = Random().nextInt().toString(),
                ident = Random().nextInt().toString()
            )
        )
        // sist_endret er ikke gammel nok, så denne skal heller ikke telles med
        innsendingRepository.antallStoppetUnderBehandling() shouldBe 0

    }

    @Test
    fun `lagre og hente bare innsending`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val innsending = Innsending(journalpostId = journalpostId, ident = ident)

        innsendingRepository.lagre(innsending)

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!

        assertEquals(innsending.journalpostId, hentetInnsending.journalpostId)
        assertEquals(innsending.id, hentetInnsending.id)
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

    @Test
    fun `sjekk optimistisk locking`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        var innsending = Innsending(journalpostId = journalpostId, ident = ident)

        innsending.sistEndret shouldBe null

        innsending = innsendingRepository.lagre(innsending)

        innsending.sistEndret shouldNotBe null

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!
        innsendingRepository.lagre(hentetInnsending)

        shouldThrowWithMessage<IllegalStateException>("Noen andre har endret denne") {
            innsendingRepository.lagre(innsending)
        }
    }

    @Test
    // TODO: Gjør om til feilHendelsen som kommer fra PDL
    fun `skal hente journalpostId for innsendinger som har feilet`() {
        /*
        val journalpostId = Random().nextInt().toString()
        val innsending = innsendingMedSkjerming(journalpostId = journalpostId)

        innsending.håndter(
            nyTiltakHendelse(
                journalpostId = journalpostId,
                tiltaksaktivitet = null,
                feil = ArenaTiltakMottattHendelse.Feilmelding.PersonIkkeFunnet,
            )
        )

        innsendingRepository.lagre(innsending)

        innsendingRepository.hentInnsendingerMedTilstandFaktainnhentingFeilet() shouldContainExactly listOf(
            journalpostId
        )

         */
    }
}
