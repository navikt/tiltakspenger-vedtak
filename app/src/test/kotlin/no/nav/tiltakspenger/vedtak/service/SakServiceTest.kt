package no.nav.tiltakspenger.vedtak.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother.brukerTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
import org.junit.jupiter.api.Test

internal class SakServiceTest {

    private val sakRepo: SakRepo = mockk()
    private val behandlingRepo: BehandlingRepo = mockk()
    private val sakService = SakServiceImpl(sakRepo, behandlingRepo)

    @Test
    fun `søknad som ikke overlapper med eksisterende sak, blir en ny sak med en behandling`() {
        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns emptyList()
        every { sakRepo.lagre(any()) } returnsArgument 0

        val søknad = nySøknadMedBrukerTiltak(
            tiltak = brukerTiltak(
                startdato = 1.januar(2023),
                sluttdato = 31.mars(2023),
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

        val søknad = nySøknadMedBrukerTiltak(
            journalpostId = "søknad1",
            tiltak = brukerTiltak(
                startdato = 1.januar(2023),
                sluttdato = 31.januar(2023),
            ),
            opprettet = 1.januarDateTime(2023),
        )
        val sak = sakService.motta(søknad)

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(sak)

        val søknad2 = nySøknadMedBrukerTiltak(
            journalpostId = "søknad2",
            tiltak = brukerTiltak(
                startdato = 1.mars(2023),
                sluttdato = 31.mars(2023),
            ),
            opprettet = 2.januarDateTime(2023),
        )
        val sak2 = sakService.motta(søknad2)

        sak2.behandlinger.size shouldBe 1
        sak.id shouldBe sak2.id
        sak2.behandlinger.filterIsInstance<BehandlingVilkårsvurdert>().first().søknad() shouldBe søknad2
    }
}
