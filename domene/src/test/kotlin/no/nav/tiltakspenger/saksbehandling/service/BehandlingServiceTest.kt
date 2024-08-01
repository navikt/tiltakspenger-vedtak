package no.nav.tiltakspenger.saksbehandling.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.TestSessionFactory
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingPåbegyntAvslag
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingPåbegyntInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterAvslag
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler123
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerMedKode6
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerMedKode7
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.AttesteringRepo
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
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
    private lateinit var tiltakGateway: TiltakGateway
    private lateinit var attesteringRepo: AttesteringRepo
    private lateinit var sakRepo: SakRepo
    private lateinit var personopplysningRepo: PersonopplysningerRepo
    private lateinit var sessionFactory: TestSessionFactory
    private lateinit var sakService: SakService
    private lateinit var søknadRepo: SøknadRepo
    private lateinit var saksoversiktRepo: SaksoversiktRepo

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk()
        vedtakRepo = mockk()
        personopplysningRepo = mockk(relaxed = true)
        utbetalingService = mockk()
        brevPublisherGateway = mockk()
        meldekortGrunnlagGateway = mockk()
        tiltakGateway = mockk()
        attesteringRepo = mockk(relaxed = true)
        sakRepo = mockk(relaxed = true)
        sessionFactory = TestSessionFactory()
        sakService = mockk(relaxed = true)
        søknadRepo = mockk(relaxed = true)
        saksoversiktRepo = mockk(relaxed = true)

        behandlingService =
            BehandlingServiceImpl(
                behandlingRepo = behandlingRepo,
                vedtakRepo = vedtakRepo,
                personopplysningRepo = personopplysningRepo,
                utbetalingService = utbetalingService,
                brevPublisherGateway = brevPublisherGateway,
                meldekortGrunnlagGateway = meldekortGrunnlagGateway,
                sakRepo = sakRepo,
                attesteringRepo = attesteringRepo,
                sessionFactory = sessionFactory,
                saksoversiktRepo = saksoversiktRepo,
            )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `ikke lov å sende en behandling til beslutter uten saksbehandler`() {
        val saksbehandler = ObjectMother.saksbehandler()
        val innvilget = behandlingPåbegyntInnvilget(saksbehandler = saksbehandler).avbrytBehandling(saksbehandler)

        shouldThrow<IllegalStateException> {
            innvilget.tilBeslutning(saksbehandler123())
        }.message shouldBe "Ikke lov å sende Behandling til Beslutter uten saksbehandler"

        shouldThrow<IkkeImplementertException> {
            val avslag = behandlingPåbegyntAvslag(saksbehandler = saksbehandler).avbrytBehandling(saksbehandler)
            avslag.tilBeslutning(saksbehandler123())
        }.message shouldBe "Støtter ikke avslag enda."
    }

    @Test
    fun `ikke lov å iverksette en behandling uten beslutter`() {
        val innvilget = behandlingTilBeslutterInnvilget(saksbehandler123())

        shouldThrow<IllegalStateException> {
            innvilget.iverksett(saksbehandler123())
        }.message shouldBe "Ikke lov å iverksette uten beslutter"

        shouldThrow<IkkeImplementertException> {
            val avslag = behandlingTilBeslutterAvslag()
            avslag.iverksett(saksbehandler123())
        }.message shouldBe "Støtter ikke avslag enda."
    }

    @Test
    fun `sjekk at man ikke kan se behandlinger for en person som er fortrolig uten tilgang`() {
        val person = listOf(ObjectMother.personopplysningKjedeligFyr(fortrolig = true))
        val behandlinger: List<Behandling> = ObjectMother.sakMedOpprettetBehandling(
            sakPersonopplysninger = SakPersonopplysninger(person),
        ).behandlinger

        every { behandlingRepo.hentAlleForIdent(any()) } returns listOf(behandlinger.first() as Førstegangsbehandling)
        every { personopplysningRepo.hent(any()) } returns SakPersonopplysninger(person)

        behandlingService.hentBehandlingForIdent(Fnr.random(), saksbehandler123()).size shouldBe 0
        behandlingService.hentBehandlingForIdent(Fnr.random(), saksbehandlerMedKode7()).size shouldBe 1
        // TODO : Er ikke dette galt?
        behandlingService.hentBehandlingForIdent(Fnr.random(), saksbehandlerMedKode6()).size shouldBe 0
    }

    @Test
    fun `sjekk at man ikke kan sende tilbake uten beslutter rolle`() {
        val behandlingId = BehandlingId.random()
        val behandling = behandlingTilBeslutterInnvilget(saksbehandler123()).copy(beslutter = beslutter().navIdent)

        every { behandlingRepo.hent(behandlingId) } returns behandling
        every { behandlingRepo.lagre(any(), any()) } returnsArgument 0

        shouldThrow<IllegalStateException> {
            behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandler123(), "begrunnelse")
        }

        shouldNotThrow<IllegalStateException> {
            behandlingService.sendTilbakeTilSaksbehandler(behandlingId, beslutter(), "begrunnelse")
        }
    }
}
