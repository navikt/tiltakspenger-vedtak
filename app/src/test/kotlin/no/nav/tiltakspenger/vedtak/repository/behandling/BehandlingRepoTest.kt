package no.nav.tiltakspenger.vedtak.repository.behandling

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.Saksnummer
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.sakMedOpprettetBehandling
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayCleanAndMigrate
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random

@Testcontainers
internal class BehandlingRepoTest {
    private val behandlingRepo: BehandlingRepo = PostgresBehandlingRepo()
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
    fun `lagre og hente en behandling`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val deltakelseFom = 1.januar(2023)
        val deltakelseTom = 31.mars(2023)
        val sakId = SakId.random()
        val sak = Sak(
            id = sakId,
            ident = ident,
            saknummer = Saksnummer(verdi = "123"),
            periode = Periode(fra = deltakelseFom, til = deltakelseTom),
            behandlinger = listOf(),
            personopplysninger = listOf(),
        )
        sakRepo.lagre(sak)

        val søknad = ObjectMother.nySøknad(
            journalpostId = journalpostId,
            personopplysninger = ObjectMother.personSøknad(
                ident = ident,
            ),
            tiltak = ObjectMother.søknadTiltak(
                deltakelseFom = deltakelseFom,
                deltakelseTom = deltakelseTom,
            ),
            barnetillegg = listOf(ObjectMother.barnetilleggMedIdent()),
        )

        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(sakId = sakId, søknad = søknad)

        behandlingRepo.lagre(behandling)

        val hentBehandling = behandlingRepo.hent(behandling.id)

        hentBehandling shouldNotBe null
    }

    @Test
    fun `lagre og hente en behandling som er vilkårsvurdert`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val deltakelseFom = 1.januar(2023)
        val deltakelseTom = 31.mars(2023)
        val sakId = SakId.random()
        val sak = Sak(
            id = sakId,
            ident = ident,
            saknummer = Saksnummer(verdi = "123"),
            periode = Periode(fra = deltakelseFom, til = deltakelseTom),
            behandlinger = listOf(),
            personopplysninger = listOf(),
        )
        sakRepo.lagre(sak)

        val søknad = ObjectMother.nySøknad(
            journalpostId = journalpostId,
            personopplysninger = ObjectMother.personSøknad(
                ident = ident,
            ),
            tiltak = ObjectMother.søknadTiltak(
                deltakelseFom = deltakelseFom,
                deltakelseTom = deltakelseTom,
            ),
            barnetillegg = listOf(ObjectMother.barnetilleggMedIdent()),
        )

        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(sakId = sakId, søknad = søknad)

        behandlingRepo.lagre(behandling)

        val hentBehandling = behandlingRepo.hent(behandling.id)
        if (hentBehandling is Søknadsbehandling.Opprettet) {
            val behandlingVilkårsvurdert = hentBehandling.vilkårsvurder()
            behandlingRepo.lagre(behandlingVilkårsvurdert)
            behandlingVilkårsvurdert shouldNotBe null
        }

        hentBehandling shouldNotBe null
    }

    @Test
    fun `hentAlleForIdent skal kun hente behandlinger for en ident og ikke de andre`() {
        val ident = Random().nextInt().toString()
        val vårSakId = SakId.random()
        val enAnnenSakId = SakId.random()
        val sakForVårIdent = sakMedOpprettetBehandling(id = vårSakId, ident = ident)
        val enAnnenSak = sakMedOpprettetBehandling(id = enAnnenSakId, ident = "random")

        sakRepo.lagre(sakForVårIdent)
        sakRepo.lagre(enAnnenSak)

        val hentBehandling = behandlingRepo.hentAlleForIdent(ident)

        hentBehandling.size shouldBe 1
    }
}
