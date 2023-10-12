package no.nav.tiltakspenger.vedtak.repository.sak

import io.kotest.matchers.shouldNotBe
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.Saksnummer
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayCleanAndMigrate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random

@Testcontainers
internal class SakRepoTest {
    private val sakRepo: SakRepo = PostgresSakRepo()

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayCleanAndMigrate()
    }

    @Test
    fun `lagre og hente en sak med en søknad`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val startDato = 1.januar(2023)
        val sluttdato = 31.mars(2023)

//        val søknad = ObjectMother.nySøknadMedBrukerTiltak(
//            journalpostId = journalpostId,
//            personopplysninger = ObjectMother.personSøknad(
//                ident = ident,
//            ),
//            tiltak = ObjectMother.brukerTiltak(
//                startdato = startDato,
//                sluttdato = sluttdato,
//            ),
//            barnetillegg = listOf(ObjectMother.barnetilleggMedIdent()),
//        )

        val sak = Sak(
            id = SakId.random(),
            ident = ident,
            saknummer = Saksnummer(verdi = "123"),
            periode = Periode(fra = startDato, til = sluttdato),
            behandlinger = listOf(),
            personopplysninger = listOf(),
        )

        sakRepo.lagre(sak)

        val hentSak = sakRepo.hentForIdentMedPeriode(ident, Periode(fra = startDato, til = sluttdato))

        hentSak shouldNotBe emptyList<Sak>()
    }
}
