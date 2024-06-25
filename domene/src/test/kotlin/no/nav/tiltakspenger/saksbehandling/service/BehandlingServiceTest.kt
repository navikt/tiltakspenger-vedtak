package no.nav.tiltakspenger.saksbehandling.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.april
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.juli
import no.nav.tiltakspenger.felles.mai
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
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
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.MultiRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BehandlingServiceTest {

    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var vedtakRepo: VedtakRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var utbetalingService: UtbetalingService
    private lateinit var brevPublisherGateway: BrevPublisherGateway
    private lateinit var meldekortGrunnlagGateway: MeldekortGrunnlagGateway
    private lateinit var multiRepo: MultiRepo
    private lateinit var sakRepo: SakRepo
    private lateinit var personopplysningRepo: PersonopplysningerRepo

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk()
        vedtakRepo = mockk()
        personopplysningRepo = mockk(relaxed = true)
        utbetalingService = mockk()
        brevPublisherGateway = mockk()
        meldekortGrunnlagGateway = mockk()
        multiRepo = mockk(relaxed = true)
        sakRepo = mockk(relaxed = true)

        behandlingService =
            BehandlingServiceImpl(
                behandlingRepo,
                vedtakRepo,
                personopplysningRepo,
                utbetalingService,
                brevPublisherGateway,
                meldekortGrunnlagGateway,
                multiRepo,
                sakRepo,
            )
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
        val behandling = Førstegangsbehandling.opprettBehandling(sakId, søknad).vilkårsvurder()
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
        val behandling = Førstegangsbehandling.opprettBehandling(sakId, søknad).vilkårsvurder()
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

        val lagretBehandling = slot<Behandling>()
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

        val lagretBehandling = slot<Behandling>()
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
            tiltak(eksternId = "før", fom = 1.januar(2022), tom = 31.mars(2022)),
            tiltak(eksternId = "slutterInni", fom = 1.mai(2022), tom = 31.januar(2023)),
            tiltak(eksternId = "starterInni", fom = 1.januar(2023), tom = 31.juli(2023)),
            tiltak(eksternId = "etter", fom = 1.april(2023), tom = 31.juli(2023)),
        )
        behandlingService.oppdaterTiltak(behandling.id, tiltak)

        lagretBehandling.captured.tiltak.tiltak.size shouldBe 3
        lagretBehandling.captured.tiltak.tiltak.first { it.eksternId == "slutterInni" }.eksternId shouldBe "slutterInni"
        lagretBehandling.captured.tiltak.tiltak.first { it.eksternId == "starterInni" }.eksternId shouldBe "starterInni"
        lagretBehandling.captured.tiltak.tiltak.first { it.eksternId == "etter" }.eksternId shouldBe "etter"
    }

    @Test
    fun `sjekk at man ikke kan se behandlinger for en person som er fortrolig uten tilgang`() {
        val person = listOf(ObjectMother.personopplysningKjedeligFyr(fortrolig = true))
        val behandlinger: List<Behandling> = ObjectMother.sakMedOpprettetBehandling(
            personopplysninger = SakPersonopplysninger(person),
        ).behandlinger

        every { behandlingRepo.hentAlleForIdent(any()) } returns listOf(behandlinger.first() as Førstegangsbehandling)
        every { personopplysningRepo.hent(any()) } returns SakPersonopplysninger(person)

        behandlingService.hentBehandlingForIdent("whatever", saksbehandler123()).size shouldBe 0
        behandlingService.hentBehandlingForIdent("whatever", saksbehandlerMedKode7()).size shouldBe 1
        // TODO : Er ikke dette galt?
        behandlingService.hentBehandlingForIdent("whatever", saksbehandlerMedKode6()).size shouldBe 0
    }

    @Test
    fun `sjekk at man ikke kan sende tilbake uten beslutter rolle`() {
        val behandlingId = BehandlingId.random()
        val behandling = behandlingTilBeslutterInnvilget().copy(beslutter = beslutter().navIdent)

        every { behandlingRepo.hent(behandlingId) } returns behandling

        shouldThrow<IllegalStateException> {
            behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandler123(), "begrunnelse")
        }

        shouldNotThrow<IllegalStateException> {
            behandlingService.sendTilbakeTilSaksbehandler(behandlingId, beslutter(), "begrunnelse")
        }
    }
}
