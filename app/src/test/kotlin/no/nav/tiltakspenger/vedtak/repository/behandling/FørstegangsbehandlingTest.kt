package no.nav.tiltakspenger.vedtak.repository.behandling

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.antallDagerFraSaksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
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

class FørstegangsbehandlingTest {

    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var vedtakRepo: VedtakRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var utbetalingService: UtbetalingService
    private lateinit var brevPublisherGateway: BrevPublisherGateway
    private lateinit var meldekortGrunnlagGateway: MeldekortGrunnlagGateway
    private lateinit var multiRepo: MultiRepo
    private lateinit var sakRepo: SakRepo
    private lateinit var personopplysningRepo: PersonopplysningerRepo

    private val saksbehandlerMedTilgang = ObjectMother.saksbehandler()
    private val saksbehandlerUtenTilgang = ObjectMother.saksbehandlerUtenTilgang()

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
    fun `når man oppdaterer antall dager skal de kun oppdateres for det tiltaket som man har oppgitt i tiltakId-parameteren`() {
        val vilkårsvurdertBehandling = ObjectMother.behandlingVilkårsvurdertInnvilget()

        val lagretBehandling = slot<Førstegangsbehandling>()
        every { behandlingRepo.hentOrNull(any()) } returns vilkårsvurdertBehandling
        every { behandlingRepo.lagre(capture(lagretBehandling)) } returnsArgument 0

        val tiltak1 = ObjectMother.tiltak(id = TiltakId.random(), fom = 1.januar(2026), tom = 31.januar(2026))
        val tiltak2 = ObjectMother.tiltak(id = TiltakId.random(), fom = 1.februar(2026), tom = 28.februar(2026))
        val tiltak = listOf(tiltak1, tiltak2)
        val vilkårsvurdertBehandlingMedToTiltak = vilkårsvurdertBehandling.oppdaterTiltak(tiltak)

        val periodisertAntallDagerVerdi = antallDagerFraSaksbehandler(
            periode = Periode(
                fra = 1.januar(2026),
                til = 15.januar(2026),
            ),
        )

        val vilkårsvurdertBehandlingMedAntallDager = vilkårsvurdertBehandlingMedToTiltak.oppdaterAntallDager(
            tiltakId = tiltak1.id,
            nyPeriodeMedAntallDager = periodisertAntallDagerVerdi,
            saksbehandler = saksbehandlerMedTilgang,
        )

        tiltak1 shouldNotBeEqual vilkårsvurdertBehandlingMedAntallDager.tiltak.get(0)
        tiltak2 shouldBeEqual vilkårsvurdertBehandlingMedAntallDager.tiltak.get(1)
    }

    @Test
    fun `det skal ikke være mulig å legge til antall dager i uken for en periode som går utenfor perioden på tiltaket`() {
        val vilkårsvurdertBehandling = ObjectMother.behandlingVilkårsvurdertInnvilget()

        val lagretBehandling = slot<Førstegangsbehandling>()
        every { behandlingRepo.hentOrNull(any()) } returns vilkårsvurdertBehandling
        every { behandlingRepo.lagre(capture(lagretBehandling)) } returnsArgument 0

        val tiltak = ObjectMother.tiltak(id = TiltakId.random(), fom = 1.januar(2026), tom = 31.januar(2026))
        val vilkårsvurdertBehandlingMedToTiltak = vilkårsvurdertBehandling.oppdaterTiltak(listOf(tiltak))

        val periodisertAntallDagerVerdi = antallDagerFraSaksbehandler(
            periode = Periode(
                fra = 1.januar(2026),
                til = 1.februar(2026),
            ),
        )

        shouldThrow<IllegalArgumentException> {
            vilkårsvurdertBehandlingMedToTiltak.oppdaterAntallDager(
                tiltakId = tiltak.id,
                nyPeriodeMedAntallDager = periodisertAntallDagerVerdi,
                saksbehandler = saksbehandlerMedTilgang,
            )
        }
    }

    @Test
    fun `det skal ikke være mulig å legge til antall dager i uken på et tiltak som ikke fins på behandlingen`() {
        val vilkårsvurdertBehandling = ObjectMother.behandlingVilkårsvurdertInnvilget()
        shouldThrowWithMessage<IllegalStateException>(
            "Kan ikke oppdatere antall dager fordi vi fant ikke tiltaket på behandlingen",
        ) {
            vilkårsvurdertBehandling.oppdaterAntallDager(
                tiltakId = TiltakId.random(),
                nyPeriodeMedAntallDager = mockk<PeriodeMedVerdi<AntallDager>>(),
                saksbehandler = saksbehandlerMedTilgang,
            )
        }
    }

    @Test
    fun `det skal ikke være mulig å legge til antall dager i uken på en behandling som er iverksatt`() {
        val iverksattBehandling = ObjectMother.behandlingInnvilgetIverksatt()
        shouldThrowWithMessage<IllegalArgumentException>(
            "Kan ikke oppdatere antall dager i tiltak, feil tilstand ${iverksattBehandling.tilstand}",
        ) {
            iverksattBehandling.oppdaterAntallDager(
                tiltakId = mockk<TiltakId>(),
                nyPeriodeMedAntallDager = mockk<PeriodeMedVerdi<AntallDager>>(),
                saksbehandler = saksbehandlerMedTilgang,
            )
        }
    }

    @Test
    fun `det skal ikke være mulig å oppdatere antall dager uten saksbehandler-tilgang`() {
        val vilkårsvurdertBehandling = ObjectMother.behandlingVilkårsvurdertInnvilget()
        shouldThrowWithMessage<IllegalStateException>(
            "Man kan ikke oppdatere antall dager uten å være saksbehandler eller admin",
        ) {
            vilkårsvurdertBehandling.oppdaterAntallDager(
                tiltakId = mockk<TiltakId>(),
                nyPeriodeMedAntallDager = mockk<PeriodeMedVerdi<AntallDager>>(),
                saksbehandler = saksbehandlerUtenTilgang,
            )
        }
    }

    @Test
    fun `det skal ikke være mulig å tilbakestille saksbehandlers opplysninger for antall dager i uken på en behandling som er iverksatt`() {
        val iverksattBehandling = ObjectMother.behandlingInnvilgetIverksatt()
        shouldThrowWithMessage<IllegalArgumentException>(
            "Kan ikke tilbakestille antall dager i tiltak, feil tilstand ${iverksattBehandling.tilstand}",
        ) {
            iverksattBehandling.tilbakestillAntallDager(
                tiltakId = mockk<TiltakId>(),
                saksbehandler = saksbehandlerMedTilgang,
            )
        }
    }

    @Test
    fun `det skal ikke være mulig å tilbakestille antall dager uten saksbehandler-tilgang`() {
        val vilkårsvurdertBehandling = ObjectMother.behandlingVilkårsvurdertInnvilget()
        shouldThrowWithMessage<IllegalStateException>(
            "Man kan ikke tilbakestille antall dager uten å være saksbehandler eller admin",
        ) {
            vilkårsvurdertBehandling.tilbakestillAntallDager(
                tiltakId = mockk<TiltakId>(),
                saksbehandler = saksbehandlerUtenTilgang,
            )
        }
    }
}
