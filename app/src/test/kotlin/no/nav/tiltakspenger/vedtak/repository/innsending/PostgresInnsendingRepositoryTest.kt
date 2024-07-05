package no.nav.tiltakspenger.vedtak.repository.innsending

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.innsending.domene.Innsending
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.barnetilleggUtenIdent
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedPersonopplysninger
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedYtelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySkjermingHendelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.personSøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.skjermingTrue
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseSak
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayCleanAndMigrate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random

@Testcontainers
internal class PostgresInnsendingRepositoryTest {
    private val innsendingRepository = PostgresInnsendingRepository()

    companion object {
        val random = Random()

        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayCleanAndMigrate()
    }

    @Test
    fun `skal telle antall innsendinger korrekt`() {
        var antallInnsendinger = innsendingRepository.antall()
        val fom = 1.januar(2022)
        val tom = 31.mars(2022)

        innsendingRepository.lagre(
            Innsending(
                journalpostId = random.nextInt().toString(),
                ident = random.nextInt().toString(),
                fom = fom,
                tom = tom,
            ),
        )
        antallInnsendinger++
        innsendingRepository.antall() shouldBe antallInnsendinger

        innsendingRepository.lagre(
            Innsending(
                journalpostId = random.nextInt().toString(),
                ident = random.nextInt().toString(),
                fom = fom,
                tom = tom,
            ),
        )
        antallInnsendinger++
        innsendingRepository.lagre(
            Innsending(
                journalpostId = random.nextInt().toString(),
                ident = random.nextInt().toString(),
                fom = fom,
                tom = tom,
            ),
        )
        antallInnsendinger++
        innsendingRepository.lagre(
            Innsending(
                journalpostId = random.nextInt().toString(),
                ident = random.nextInt().toString(),
                fom = fom,
                tom = tom,
            ),
        )
        antallInnsendinger++
        innsendingRepository.antall() shouldBe antallInnsendinger
    }

    @Test
    fun `skal telle antall innsendinger hvor faktainnhenting har feilet korrekt`() {
        // Flyway legger inn 6, ingen har feilet.
        // Dette er mao en litt slapp test,
        // men sjekker iallefall om spørringen feiler eller ikke..
        innsendingRepository.antallMedTilstandFaktainnhentingFeilet() shouldBe 0
    }

    @Test
    @Disabled("Denne baserer seg på de lokale migreringene som ikke er klare nå")
    fun `skal telle antall innsendinger hvor behandlingen ikke er ferdig`() {
        // Flyway legger inn 6, alle er ferdig.
        // Dette er mao en litt slapp test,
        // men sjekker iallefall om spørringen feiler eller ikke..
        innsendingRepository.antallStoppetUnderBehandling() shouldBe 1

        val fom = 1.januar(2022)
        val tom = 31.mars(2022)

        innsendingRepository.lagre(
            Innsending(
                journalpostId = random.nextInt().toString(),
                ident = random.nextInt().toString(),
                fom = fom,
                tom = tom,
            ),
        )
        // sist_endret er ikke gammel nok, så denne skal heller ikke telles med
        innsendingRepository.antallStoppetUnderBehandling() shouldBe 1
    }

    @Test
    fun `lagre og hente bare innsending`() {
        val journalpostId = random.nextInt().toString()
        val ident = random.nextInt().toString()
        val fom = 1.januar(2022)
        val tom = 31.mars(2022)
        val innsending = Innsending(
            journalpostId = journalpostId,
            ident = ident,
            fom = fom,
            tom = tom,
        )

        innsendingRepository.lagre(innsending)

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!

        assertEquals(innsending.journalpostId, hentetInnsending.journalpostId)
        assertEquals(innsending.id, hentetInnsending.id)
        assertEquals(innsending.tilstand, hentetInnsending.tilstand)
        innsending.personopplysninger shouldBe null
    }

    @Test
    fun `lagre og hente hele aggregatet med ArenaTiltak`() {
        val ident = random.nextInt().toString()
        val journalpostId = random.nextInt().toString()

        val søknad = ObjectMother.nySøknad(
            periode = Periode(1.januar(2022), 31.januar(2022)),
            journalpostId = journalpostId,
            personopplysninger = personSøknad(
                ident = ident,
            ),
            barnetillegg = listOf(barnetilleggUtenIdent()),
        )
        val personopplysninger = personopplysningKjedeligFyr(ident = ident, strengtFortroligUtland = false)
        val ytelseSak = listOf(ytelseSak())

        val innsending = innsendingMedYtelse(
            journalpostId = journalpostId,
            ident = ident,
            søknad = søknad,
            personopplysninger = listOf(personopplysninger),
            skjerming = skjermingTrue(ident = ident),
            ytelseSak = ytelseSak,
        )

        innsendingRepository.lagre(innsending)

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!

        assertEquals(innsending.journalpostId, hentetInnsending.journalpostId)
        assertEquals(innsending.ident, hentetInnsending.ident)
        assertEquals(innsending.id, hentetInnsending.id)
        assertEquals(innsending.tilstand, hentetInnsending.tilstand)
        assertEquals(innsending.fom, hentetInnsending.fom)
        assertEquals(innsending.tom, hentetInnsending.tom)
        hentetInnsending.personopplysninger!!.personopplysningerliste shouldBe listOf(personopplysninger.copy(skjermet = true))
        hentetInnsending.ytelser!!.ytelserliste shouldContainExactly ytelseSak
        hentetInnsending.aktivitetslogg shouldBeEqualToComparingFields innsending.aktivitetslogg
    }

    @Test
    fun `lagre og hente hele aggregatet med Skjerming`() {
        val ident = random.nextInt().toString()
        val journalpostId = random.nextInt().toString()

        val søknad = ObjectMother.nySøknad(
            journalpostId = journalpostId,
            personopplysninger = personSøknad(
                ident = ident,
            ),
            barnetillegg = listOf(barnetilleggUtenIdent()),
        )
        val personopplysninger = personopplysningKjedeligFyr(ident = ident, strengtFortroligUtland = false)

        val innsending = innsendingMedPersonopplysninger(
            journalpostId = journalpostId,
            ident = ident,
            søknad = søknad,
            personopplysninger = listOf(personopplysninger),
        )

        innsendingRepository.lagre(innsending)

        innsending.håndter(
            skjermingMottattHendelse = nySkjermingHendelse(
                journalpostId = journalpostId,
                skjerming = skjermingTrue(),
            ),
        )

        innsendingRepository.lagre(innsending)

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!

        assertEquals(innsending.journalpostId, hentetInnsending.journalpostId)
        assertEquals(innsending.ident, hentetInnsending.ident)
        assertEquals(innsending.id, hentetInnsending.id)
        assertEquals(innsending.tilstand, hentetInnsending.tilstand)
        assertEquals(innsending.fom, hentetInnsending.fom)
        assertEquals(innsending.tom, hentetInnsending.tom)
        hentetInnsending.personopplysninger!!.personopplysningerliste shouldBe listOf(personopplysninger.copy(skjermet = true))
        hentetInnsending.aktivitetslogg shouldBeEqualToComparingFields innsending.aktivitetslogg
    }

    @Test
    fun `sjekk optimistisk locking`() {
        val journalpostId = random.nextInt().toString()
        val ident = random.nextInt().toString()
        val fom = 1.januar(2022)
        val tom = 31.mars(2022)
        var innsending = Innsending(
            journalpostId = journalpostId,
            ident = ident,
            fom = fom,
            tom = tom,
        )

        innsending.sistEndret shouldBe null

        innsending = innsendingRepository.lagre(innsending)

        innsending.sistEndret shouldNotBe null

        val hentetInnsending = innsendingRepository.hent(journalpostId)!!
        innsendingRepository.lagre(hentetInnsending)

        shouldThrowWithMessage<IllegalStateException>("Noen andre har endret denne") {
            innsendingRepository.lagre(innsending)
        }
    }

    // TODO: Gjør om til feilHendelsen som kommer fra PDL
    @Test
    fun `skal hente journalpostId for innsendinger som har feilet`() {
        /*
        val journalpostId = random.nextInt().toString()
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
