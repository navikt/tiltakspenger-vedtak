package no.nav.tiltakspenger.vedtak.repository.sak

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother.tomSak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
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
        val ident = Random().nextInt().toString()
        val startdato = 1.januar(2023)
        val sluttdato = 31.mars(2023)

        val sak = tomSak(ident = ident, periode = Periode(fra = startdato, til = sluttdato))

        sakRepo.lagre(sak)

        val hentSak = sakRepo.hentForIdentMedPeriode(ident, Periode(fra = startdato, til = sluttdato))

        hentSak shouldNotBe emptyList<Sak>()
    }

    @Test
    fun `hentForSaksnummer skal hente sak med matchende saksnummer`() {
        val saksnummerMock = Saksnummer(verdi = "123456789")
        val sak = tomSak(saksnummer = saksnummerMock)
        sakRepo.lagre(sak)
        val sakHentetUtifraSaksnummer = sakRepo.hentForSaksnummer(saksnummer = saksnummerMock.verdi)
        sakHentetUtifraSaksnummer?.saknummer shouldBe sak.saknummer
    }

    @Test
    fun `hentForIdent skal hente saker med matchende ident`() {
        val ident = "123"
        val sak1 = tomSak(ident = ident).also { sakRepo.lagre(it) }
        val sak2 = tomSak(ident = ident).also { sakRepo.lagre(it) }
        val sak3 = tomSak(ident = "456").also { sakRepo.lagre(it) }

        val sakerMedIdent = sakRepo.hentForIdent(ident)
        sakerMedIdent.size shouldBe 2
        sakerMedIdent shouldContain sak1
        sakerMedIdent shouldContain sak2
        sakerMedIdent shouldNotContain sak3
    }
}
