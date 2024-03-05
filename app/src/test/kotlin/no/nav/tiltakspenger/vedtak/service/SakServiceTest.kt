package no.nav.tiltakspenger.vedtak.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingInnvilgetIverksatt
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.tomSak
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.service.sak.SakService
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
import no.nav.tiltakspenger.vedtak.service.vedtak.VedtakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SakServiceTest {
    private lateinit var sakRepo: SakRepo
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var vedtakService: VedtakService
    private lateinit var sakService: SakService
    private lateinit var attesteringRepo: AttesteringRepo

    @BeforeEach
    fun setup() {
        sakRepo = mockk()
        behandlingRepo = mockk()
        vedtakService = mockk()
        attesteringRepo = mockk()
        behandlingService = BehandlingServiceImpl(behandlingRepo, vedtakService, attesteringRepo, sakRepo)
        sakService = SakServiceImpl(sakRepo, behandlingRepo, behandlingService)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `søknad som ikke overlapper med eksisterende sak, blir en ny sak med en behandling`() {
        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns emptyList()
        every { sakRepo.lagre(any()) } returnsArgument 0

        val søknad = nySøknad(
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.mars(2023),
            ),
        )

        val sak = sakService.motta(søknad)

        sak.behandlinger.size shouldBe 1
        sak.behandlinger.first() shouldBe beInstanceOf<BehandlingVilkårsvurdert>()

        val behandling = sak.behandlinger.filterIsInstance<BehandlingVilkårsvurdert>().first()
        behandling.vurderingsperiode shouldBe Periode(1.januar(2023), 31.mars(2023))
        behandling.søknader.first() shouldBe søknad
    }

    @Test
    fun `søknad som overlapper med eksisterende sak, legger søknaden til i behandlingen`() {
        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns emptyList()
        every { sakRepo.lagre(any()) } returnsArgument 0

        val søknad = nySøknad(
            journalpostId = "søknad1",
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.januar(2023),
            ),
            opprettet = 1.januarDateTime(2023),
        )
        val sak = sakService.motta(søknad)

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(sak)

        val søknad2 = nySøknad(
            journalpostId = "søknad2",
            tiltak = søknadTiltak(
                deltakelseFom = 1.mars(2023),
                deltakelseTom = 31.mars(2023),
            ),
            opprettet = 2.januarDateTime(2023),
        )
        val sak2 = sakService.motta(søknad2)

        sak2.behandlinger.size shouldBe 1
        sak.id shouldBe sak2.id
        sak2.behandlinger.filterIsInstance<BehandlingVilkårsvurdert>().first()
            .søknad() shouldBe søknad2.copy(opprettet = søknad.opprettet)
    }

    @Test
    fun `legger til ny søknad med flere iverksatte, lager 1 ny behandling`() {
        val eksisterendeSak = tomSak(
            periode = Periode(
                1.januar(2023),
                31.mars(2023),
            ),
            behandlinger = listOf(
                behandlingInnvilgetIverksatt(),
                behandlingInnvilgetIverksatt(),
            ),
        )

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(eksisterendeSak)
        every { sakRepo.lagre(any()) } returnsArgument 0

        val søknad = nySøknad(
            journalpostId = "søknad1",
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.januar(2023),
            ),
            opprettet = 1.januarDateTime(2023),
        )

        val sak = sakService.motta(søknad)

        sak.behandlinger.size shouldBe 3
    }

    @Test
    fun `legger til ny søknad med flere iverksatte og 1 tilbeslutter, legger søknad til den åpne behandlingen`() {
        val eksisterendeSak = tomSak(
            periode = Periode(
                1.januar(2023),
                31.mars(2023),
            ),
            behandlinger = listOf(
                behandlingInnvilgetIverksatt(),
                behandlingInnvilgetIverksatt(),
                behandlingTilBeslutterInnvilget(),
            ),
        )

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(eksisterendeSak)
        every { sakRepo.lagre(any()) } returnsArgument 0

        val nyJournalpostId = "ny og fin journalpostid"
        val søknad = nySøknad(
            journalpostId = nyJournalpostId,
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.januar(2023),
            ),
            opprettet = 1.januarDateTime(2023),
        )

        val sak = sakService.motta(søknad)

        sak.behandlinger.size shouldBe 3

        val åpenBehandlinger = sak.behandlinger.filterNot { it is BehandlingIverksatt }

        åpenBehandlinger.size shouldBe 1
        val b = åpenBehandlinger.filterIsInstance<BehandlingVilkårsvurdert>().first()
        b.søknader.size shouldBe 2
        b.søknad().journalpostId shouldBe nyJournalpostId
    }
}
