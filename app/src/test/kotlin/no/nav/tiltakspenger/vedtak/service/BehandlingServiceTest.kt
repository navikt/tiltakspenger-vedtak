package no.nav.tiltakspenger.vedtak.service

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BehandlingServiceTest {

    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var behandlingService: BehandlingService

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk()
        behandlingService = BehandlingServiceImpl(behandlingRepo)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `legg til saksopplysning fra saksbehandler legger seg til i tillegg`() {
        val sakId = SakId.random()
        val søknad = ObjectMother.nySøknad(
            tiltak = ObjectMother.søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.mars(2023),
            ),
        )
        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(sakId, søknad).vilkårsvurder()
        val lagretBehandling = slot<Søknadsbehandling>()
        every { behandlingRepo.hent(any()) } returns behandling
        every { behandlingRepo.lagre(capture(lagretBehandling)) } returnsArgument 0

        val saksopplysning = Saksopplysning(
            fom = 1.februar(2023),
            tom = 28.februar(2023),
            kilde = Kilde.SAKSB,
            vilkår = Vilkår.AAP,
            detaljer = "",
            typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
            saksbehandler = "Z999999",
        )
        behandlingService.leggTilSaksopplysning(behandling.id, saksopplysning)

        lagretBehandling.captured.saksopplysninger.single { it.vilkår == Vilkår.AAP && it.kilde == Kilde.ARENA }.let {
            it.fom shouldBe 1.januar(2023)
            it.tom shouldBe 31.mars(2023)
            it.typeSaksopplysning shouldBe TypeSaksopplysning.IKKE_INNHENTET_ENDA
        }
        lagretBehandling.captured.saksopplysninger.single { it.vilkår == Vilkår.AAP && it.kilde == Kilde.SAKSB }.let {
            it.fom shouldBe 1.februar(2023)
            it.tom shouldBe 28.februar(2023)
            it.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE
        }
    }

    @Test
    fun `legg til saksopplysning som ikke er saksbehandler erstatter den gamle`() {
        val sakId = SakId.random()
        val søknad = ObjectMother.nySøknad(
            tiltak = ObjectMother.søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.mars(2023),
            ),
        )
        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(sakId, søknad).vilkårsvurder()
        val lagretBehandling = slot<Søknadsbehandling>()
        every { behandlingRepo.hent(any()) } returns behandling
        every { behandlingRepo.lagre(capture(lagretBehandling)) } returnsArgument 0

        val saksopplysning = Saksopplysning(
            fom = 1.januar(2023),
            tom = 31.mars(2023),
            kilde = Kilde.ARENA,
            vilkår = Vilkår.AAP,
            detaljer = "",
            typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
            saksbehandler = "Z999999",
        )
        behandlingService.leggTilSaksopplysning(behandling.id, saksopplysning)

        lagretBehandling.captured.saksopplysninger.filter { it.vilkår == Vilkår.AAP }.size shouldBe 1
        lagretBehandling.captured.saksopplysninger.single { it.vilkår == Vilkår.AAP && it.kilde == Kilde.ARENA }.let {
            it.fom shouldBe 1.januar(2023)
            it.tom shouldBe 31.mars(2023)
            it.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE
        }
    }

    @Test
    fun `hvis saksopplysning har en annen verdi enn den orginale skal saksbehandler fjernes`() {
        val behandling = ObjectMother.behandlingVilkårsvurdert(
            periode = Periode(1.januar(2023), 31.mars(2023)),
        ).leggTilSaksopplysning(
            Saksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.AAP,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                saksbehandler = "Z999999",
            ),
        ).behandling

        val lagretBehandling = slot<Søknadsbehandling>()
        every { behandlingRepo.hent(any()) } returns behandling
        every { behandlingRepo.lagre(capture(lagretBehandling)) } returnsArgument 0

        val saksopplysning = Saksopplysning(
            fom = 1.februar(2023),
            tom = 28.februar(2023),
            kilde = Kilde.ARENA,
            vilkår = Vilkår.AAP,
            detaljer = "",
            typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
            saksbehandler = "Z999999",
        )
        behandlingService.leggTilSaksopplysning(behandling.id, saksopplysning)

        lagretBehandling.captured.saksopplysninger.filter { it.vilkår == Vilkår.AAP }.size shouldBe 1
        lagretBehandling.captured.saksopplysninger.single { it.vilkår == Vilkår.AAP && it.kilde == Kilde.ARENA }.let {
            it.fom shouldBe 1.februar(2023)
            it.tom shouldBe 28.februar(2023)
            it.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE
        }
    }

    @Test
    fun `hvis saksopplysning har samme verdi som den orginale skal saksbehandler ikke fjernes`() {
        val behandling = ObjectMother.behandlingVilkårsvurdert(
            periode = Periode(1.januar(2023), 31.mars(2023)),
        ).leggTilSaksopplysning(
            Saksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.AAP,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                saksbehandler = "Z999999",
            ),
        ).behandling

        val lagretBehandling = slot<Søknadsbehandling>()
        every { behandlingRepo.hent(any()) } returns behandling
        every { behandlingRepo.lagre(capture(lagretBehandling)) } returnsArgument 0

        val saksopplysning = Saksopplysning(
            fom = 1.januar(2023),
            tom = 31.mars(2023),
            kilde = Kilde.ARENA,
            vilkår = Vilkår.AAP,
            detaljer = "",
            typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
            saksbehandler = null,
        )
        behandlingService.leggTilSaksopplysning(behandling.id, saksopplysning)

        lagretBehandling.captured.saksopplysninger.filter { it.vilkår == Vilkår.AAP }.size shouldBe 2
        lagretBehandling.captured.saksopplysninger.single { it.vilkår == Vilkår.AAP && it.kilde == Kilde.ARENA }.let {
            it.fom shouldBe 1.januar(2023)
            it.tom shouldBe 31.mars(2023)
            it.typeSaksopplysning shouldBe TypeSaksopplysning.IKKE_INNHENTET_ENDA
        }
        lagretBehandling.captured.saksopplysninger.single { it.vilkår == Vilkår.AAP && it.kilde == Kilde.SAKSB }.let {
            it.fom shouldBe 1.januar(2023)
            it.tom shouldBe 31.mars(2023)
            it.typeSaksopplysning shouldBe TypeSaksopplysning.HAR_YTELSE
        }
    }
}
