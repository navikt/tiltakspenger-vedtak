package no.nav.tiltakspenger.vedtak.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother.brukerTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedUføre
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseSakAAP
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
import org.junit.jupiter.api.Test

internal class SakServiceTest {

    val sakRepo: SakRepo = mockk()
    val sakService = SakServiceImpl(sakRepo)

    @Test
    fun `søknad som ikke overlapper med eksisterende sak, blir en ny sak med en behandling`() {
        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns emptyList()
        every { sakRepo.save(any()) } returnsArgument 0

        val søknad = nySøknadMedBrukerTiltak(
            tiltak = brukerTiltak(
                startdato = 1.januar(2023),
                sluttdato = 31.mars(2023),
            ),
        )

        val sak = sakService.motta(søknad)

        sak.behandlinger.size shouldBe 1
        sak.behandlinger.first() shouldBe beInstanceOf<Søknadsbehandling.Opprettet>()

        val behandling = sak.behandlinger.filterIsInstance<Søknadsbehandling.Opprettet>().first()
        behandling.vurderingsperiode shouldBe Periode(1.januar(2023), 31.mars(2023))
        behandling.søknader.first() shouldBe søknad
    }

    @Test
    fun `søknad som overlapper med eksisterende sak, legger søknaden til i behandlingen`() {
        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns emptyList()
        every { sakRepo.save(any()) } returnsArgument 0

        val søknad = nySøknadMedBrukerTiltak(
            journalpostId = "søknad1",
            tiltak = brukerTiltak(
                startdato = 1.januar(2023),
                sluttdato = 31.januar(2023),
            ),
        )
        val sak = sakService.motta(søknad)

        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns listOf(sak)

        val søknad2 = nySøknadMedBrukerTiltak(
            journalpostId = "søknad2",
            tiltak = brukerTiltak(
                startdato = 1.mars(2023),
                sluttdato = 31.mars(2023),
            ),
        )
        val sak2 = sakService.motta(søknad2)

        sak2.behandlinger.size shouldBe 1
        sak.id shouldBe sak2.id
        sak2.behandlinger.filterIsInstance<Søknadsbehandling.Opprettet>().first().søknad() shouldBe søknad2
    }

    @Test
    fun `søknad med AAP i deler av perioden blir DelvisInnvilget`() {
        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns emptyList()
        every { sakRepo.save(any()) } returnsArgument 0

        val søknad = nySøknadMedBrukerTiltak(
            tiltak = brukerTiltak(
                startdato = 1.januar(2023),
                sluttdato = 31.mars(2023),
            ),
        )

        val innsending = innsendingMedUføre(
            søknad = søknad,
            ytelseSak = ytelseSakAAP(
                fom = 1.januarDateTime(2023),
                tom = 31.januarDateTime(2023),
            ),
        )

        val sak = sakService.mottaInnsending(innsending)
        sak shouldNotBe null
//        sak.behandlinger.first() shouldBe beInstanceOf<BehandlingVilkårsvurdert.DelvisInnvilget>()
//        val behandling = sak.behandlinger.filterIsInstance<BehandlingVilkårsvurdert.DelvisInnvilget>().first()
//        behandling.vurderingsperiode shouldBe Periode(1.januar(2023), 31.mars(2023))
//        behandling.søknader.first() shouldBe søknad
//        behandling.saksopplysning shouldContain Saksopplysning.Aap(
//            fom = 1.januar(2023),
//            tom = 31.januar(2023),
//            Vilkår.AAP,
//            Kilde.ARENA,
//            "",
//            opphørTidligereSaksopplysning = false,
//            typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
//        )
    }

    @Test
    fun `mottak av søknad happypath`() {
        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns emptyList()
        every { sakRepo.save(any()) } returnsArgument 0

        val søknad = nySøknadMedBrukerTiltak()
        val sak = sakService.motta(søknad)

        sak shouldNotBe null // TODO sjekk flere felter i sak
        sak.behandlinger.size shouldBe 1

        val opprettetBehandling = sak.behandlinger.filterIsInstance<Søknadsbehandling.Opprettet>().first()

        val vilkårsvurdertBehandling =
            opprettetBehandling.vilkårsvurder(emptyList()) as BehandlingVilkårsvurdert.Innvilget

        val saksbehandler =
            Saksbehandler(navIdent = "ident", brukernavn = "navn", epost = "epost", roller = emptyList())
        val iverksattBehandling = vilkårsvurdertBehandling.iverksett(saksbehandler = saksbehandler)

        iverksattBehandling shouldBe beInstanceOf<BehandlingIverksatt>()
//        iverksattBehandling.vedtak.first() shouldBe beInstanceOf<Vedtak>()
    }
}
