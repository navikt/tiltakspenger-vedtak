package no.nav.tiltakspenger.vedtak.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.domene.vilkår.vilkårsvurder
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.april
import no.nav.tiltakspenger.felles.desember
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.juli
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterAvslag
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingVilkårsvurdertAvslag
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingVilkårsvurdertInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler123
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerMedKode6
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerMedKode7
import no.nav.tiltakspenger.objectmothers.ObjectMother.tiltak
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.service.personopplysning.PersonopplysningService
import no.nav.tiltakspenger.vedtak.service.vedtak.VedtakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class BehandlingServiceTest {

    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var vedtakService: VedtakService
    private lateinit var attesteringRepo: AttesteringRepo
    private lateinit var personopplysningService: PersonopplysningService

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk()
        vedtakService = mockk()
        attesteringRepo = mockk()
        personopplysningService = mockk(relaxed = true)
        behandlingService =
            BehandlingServiceImpl(behandlingRepo, vedtakService, attesteringRepo, personopplysningService)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `ikke lov å sende en behandling til beslutter uten saksbehandler`() {
        val innvilget = behandlingVilkårsvurdertInnvilget()
        val avslag = behandlingVilkårsvurdertAvslag()

        shouldThrow<IllegalStateException> {
            innvilget.tilBeslutting(saksbehandler123())
        }.message shouldBe "Ikke lov å sende Behandling til Beslutter uten saksbehandler"

        shouldThrow<IllegalStateException> {
            avslag.tilBeslutting(saksbehandler123())
        }.message shouldBe "Ikke lov å sende Behandling til Beslutter uten saksbehandler"
    }

    @Test
    fun `ikke lov å iverksette en behandling uten beslutter`() {
        val innvilget = behandlingTilBeslutterInnvilget()
        val avslag = behandlingTilBeslutterAvslag()

        shouldThrow<IllegalStateException> {
            innvilget.iverksett(saksbehandler123())
        }.message shouldBe "Ikke lov å iverksette uten beslutter"

        shouldThrow<IllegalStateException> {
            avslag.iverksett(saksbehandler123())
        }.message shouldBe "Ikke lov å iverksette uten beslutter"
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
        val behandling = BehandlingOpprettet.opprettBehandling(sakId, søknad).vilkårsvurder()
        val lagretBehandling = slot<Førstegangsbehandling>()
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
        val behandling = BehandlingOpprettet.opprettBehandling(sakId, søknad).vilkårsvurder()
        val lagretBehandling = slot<Førstegangsbehandling>()
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
        val behandling = ObjectMother.behandlingVilkårsvurdertInnvilget(
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

        val lagretBehandling = slot<Førstegangsbehandling>()
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
        val behandling = ObjectMother.behandlingVilkårsvurdertInnvilget(
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

        val lagretBehandling = slot<Førstegangsbehandling>()
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

    @Test
    fun `tiltak utenfor vurderingsperioden skal filtreres bort`() {
        val behandling = ObjectMother.behandlingVilkårsvurdertInnvilget(
            periode = Periode(1.januar(2023), 31.mars(2023)),
        )

        val lagretBehandling = slot<Førstegangsbehandling>()
        every { behandlingRepo.hent(any()) } returns behandling
        every { behandlingRepo.lagre(capture(lagretBehandling)) } returnsArgument 0

        val tiltak = listOf(
            tiltak(id = "før", fom = 1.januar(2022), tom = 31.desember(2022)),
            tiltak(id = "slutterInni", fom = 1.januar(2022), tom = 31.januar(2023)),
            tiltak(id = "starterInni", fom = 1.januar(2023), tom = 31.juli(2023)),
            tiltak(id = "etter", fom = 1.april(2023), tom = 31.juli(2023)),
        )
        behandlingService.oppdaterTiltak(behandling.id, tiltak)

        lagretBehandling.captured.tiltak.size shouldBe 2
        lagretBehandling.captured.tiltak.first { it.id == "slutterInni" }.id shouldBe "slutterInni"
        lagretBehandling.captured.tiltak.first { it.id == "starterInni" }.id shouldBe "starterInni"
    }

    @Test
    fun `sjekk at man ikke kan se behandlinger for en person som er fortrolig uten tilgang`() {
        val person = listOf(ObjectMother.personopplysningKjedeligFyr(fortrolig = true))
        val behandlinger: List<Behandling> = ObjectMother.sakMedOpprettetBehandling(
            personopplysninger = SakPersonopplysninger(person),
        ).behandlinger

        every { behandlingRepo.hentAlleForIdent(any()) } returns listOf(behandlinger.first() as Førstegangsbehandling)
        every { personopplysningService.hent(any()) } returns SakPersonopplysninger(person)

        behandlingService.hentBehandlingForIdent("whatever", saksbehandler123()).size shouldBe 0
        behandlingService.hentBehandlingForIdent("whatever", saksbehandlerMedKode7()).size shouldBe 1
        // TODO : Er ikke dette galt?
        behandlingService.hentBehandlingForIdent("whatever", saksbehandlerMedKode6()).size shouldBe 0
    }

    @Test
    @Disabled("Fungerer ikke fordi vi har transaksjonskode i BehandlingServiceImpl")
    fun `sjekk at man ikke kan sende inn uten beslutter rolle`() {
        val behandlingId = BehandlingId.random()
        val behandling = behandlingTilBeslutterInnvilget()

        every { behandlingRepo.hent(behandlingId) } returns behandling

        shouldThrow<IllegalStateException> {
            behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandler123(), "begrunnelse")
        }

        shouldNotThrow<IllegalStateException> {
            behandlingService.sendTilbakeTilSaksbehandler(behandlingId, beslutter(), "begrunnelse")
        }
    }
}
