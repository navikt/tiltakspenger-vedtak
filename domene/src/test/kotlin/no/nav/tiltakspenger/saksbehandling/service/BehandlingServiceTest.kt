package no.nav.tiltakspenger.saksbehandling.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.TestSessionFactory
import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterAvslag
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingUnderBehandlingAvslag
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingUnderBehandlingInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.godkjentAttestering
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler123
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerUtenTilgang
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortgrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BehandlingServiceTest {
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var vedtakRepo: VedtakRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var brevPublisherGateway: BrevPublisherGateway
    private lateinit var meldekortGrunnlagGateway: MeldekortgrunnlagGateway
    private lateinit var tiltakGateway: TiltakGateway
    private lateinit var sakRepo: SakRepo
    private lateinit var personopplysningRepo: PersonopplysningerRepo
    private lateinit var sessionFactory: TestSessionFactory
    private lateinit var sakService: SakService
    private lateinit var søknadRepo: SøknadRepo
    private lateinit var saksoversiktRepo: SaksoversiktRepo
    private lateinit var statistikkSakRepo: StatistikkSakRepo
    private lateinit var statistikkStønadRepo: StatistikkStønadRepo

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk(relaxed = true)
        vedtakRepo = mockk()
        personopplysningRepo = mockk(relaxed = true)
        brevPublisherGateway = mockk()
        meldekortGrunnlagGateway = mockk()
        tiltakGateway = mockk()
        sakRepo = mockk(relaxed = true)
        sessionFactory = TestSessionFactory()
        sakService = mockk(relaxed = true)
        søknadRepo = mockk(relaxed = true)
        saksoversiktRepo = mockk(relaxed = true)
        statistikkSakRepo = mockk(relaxed = true)
        statistikkStønadRepo = mockk(relaxed = true)

        behandlingService =
            BehandlingServiceImpl(
                behandlingRepo = behandlingRepo,
                vedtakRepo = vedtakRepo,
                personopplysningRepo = personopplysningRepo,
                brevPublisherGateway = brevPublisherGateway,
                meldekortGrunnlagGateway = meldekortGrunnlagGateway,
                sakRepo = sakRepo,
                sessionFactory = sessionFactory,
                saksoversiktRepo = saksoversiktRepo,
                statistikkSakRepo = statistikkSakRepo,
                statistikkStønadRepo = statistikkStønadRepo,
            )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `ikke lov å sende en behandling til beslutter uten saksbehandler`() {
        val saksbehandler = ObjectMother.saksbehandler()
        val innvilget =
            behandlingUnderBehandlingInnvilget(saksbehandler = saksbehandler)
                .taSaksbehandlerAvBehandlingen(saksbehandler)

        shouldThrow<IllegalStateException> {
            innvilget.tilBeslutning(saksbehandler123())
        }.message.shouldContain(
            "Behandlingen må være under behandling, det innebærer også at en saksbehandler må ta saken før den kan sendes til beslutter. Behandlingsstatus: KLAR_TIL_BEHANDLING.",
        )

        shouldThrow<IkkeImplementertException> {
            val avslag =
                behandlingUnderBehandlingAvslag(saksbehandler = saksbehandler).taSaksbehandlerAvBehandlingen(
                    saksbehandler,
                )
            avslag.tilBeslutning(saksbehandler123())
        }.message shouldBe "Støtter ikke avslag enda."
    }

    @Test
    fun `ikke lov å iverksette en behandling uten beslutter`() {
        val innvilget = behandlingTilBeslutterInnvilget(saksbehandler123())

        shouldThrow<IllegalStateException> {
            innvilget.iverksett(saksbehandler123(), godkjentAttestering())
        }.message shouldBe "Må ha status UNDER_BESLUTNING for å iverksette. Behandlingsstatus: KLAR_TIL_BESLUTNING"

        shouldThrow<IkkeImplementertException> {
            val avslag = behandlingTilBeslutterAvslag()
            avslag.iverksett(saksbehandler123(), godkjentAttestering())
        }.message shouldBe "Støtter ikke avslag enda."
    }

    @Test
    fun `må ha beslutterrolle for å ta behandling som er til beslutning`() {
        val behandlingId = BehandlingId.random()
        val saksbehandler = saksbehandler123()
        val behandling = behandlingTilBeslutterInnvilget(saksbehandler)

        every { behandlingRepo.hent(behandlingId) } returns behandling
        every { behandlingRepo.lagre(any(), any()) } returnsArgument 0
        every { personopplysningRepo.hent(any()) } returns
            SakPersonopplysninger(
                listOf(
                    personopplysningKjedeligFyr(fnr = behandling.fnr),
                ),
            )

        shouldThrow<IllegalStateException> {
            behandlingService.taBehandling(behandlingId, saksbehandlerUtenTilgang())
        }.message shouldBe
            "Saksbehandler må ha beslutterrolle. Utøvende saksbehandler: Saksbehandler(navIdent='Z12345', brukernavn='*****', epost='*****', roller=[])"
        shouldNotThrow<IllegalStateException> {
            behandlingService.taBehandling(behandlingId, beslutter())
        }
    }

    @Test
    fun `sjekk at man ikke kan sende tilbake uten beslutter rolle`() {
        val behandlingId = BehandlingId.random()
        val navIdentSaksbehandler = "A12345"
        val saksbehandler = saksbehandler(navIdent = navIdentSaksbehandler)
        val navIdentBeslutter = "B12345"
        val beslutter = beslutter(navIdent = navIdentBeslutter)
        val behandling = behandlingTilBeslutterInnvilget(saksbehandler).taBehandling(beslutter)

        every { behandlingRepo.hent(behandlingId) } returns behandling
        every { behandlingRepo.lagre(any(), any()) } returnsArgument 0
        every { personopplysningRepo.hent(any()) } returns
            SakPersonopplysninger(
                listOf(
                    personopplysningKjedeligFyr(fnr = behandling.fnr),
                ),
            )
        shouldThrow<IllegalStateException> {
            behandlingService.taBehandling(behandlingId, saksbehandlerUtenTilgang(navIdent = navIdentBeslutter))
        }.message shouldBe
            "Saksbehandler må ha beslutterrolle. Utøvende saksbehandler: Saksbehandler(navIdent='B12345', brukernavn='*****', epost='*****', roller=[])"

        shouldNotThrow<IllegalStateException> {
            behandlingService.sendTilbakeTilSaksbehandler(behandlingId, beslutter(navIdent = navIdentBeslutter), "begrunnelse")
        }
    }
}
