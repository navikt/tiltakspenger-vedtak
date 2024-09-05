package no.nav.tiltakspenger.saksbehandling.service

import arrow.core.getOrElse
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.Rolle.SAKSBEHANDLER
import no.nav.tiltakspenger.libs.common.Rolle.SKJERMING
import no.nav.tiltakspenger.libs.common.Roller
import no.nav.tiltakspenger.libs.common.TestSessionFactory
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.barn
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingUnderBehandlingUavklart
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.vedtak.RammevedtakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SakServiceTest {
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var vedtakRepo: RammevedtakRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var vedtakService: RammevedtakService
    private lateinit var brevPublisherGateway: BrevPublisherGateway
    private lateinit var meldekortRepo: MeldekortRepo
    private lateinit var tiltakGateway: TiltakGateway
    private lateinit var personopplysningRepo: PersonopplysningerRepo
    private lateinit var sakRepo: SakRepo
    private lateinit var sakService: SakService
    private lateinit var personGateway: PersonGateway
    private lateinit var skjermingGateway: SkjermingGateway
    private lateinit var søknadRepo: SøknadRepo
    private lateinit var saksoversiktRepo: SaksoversiktRepo
    private lateinit var statistikkSakRepo: StatistikkSakRepo
    private lateinit var statistikkStønadRepo: StatistikkStønadRepo

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk()
        vedtakRepo = mockk()
        vedtakService = mockk()
        brevPublisherGateway = mockk()
        meldekortRepo = mockk()
        tiltakGateway = mockk()
        sakRepo = mockk()
        personopplysningRepo = mockk(relaxed = true)
        personGateway = mockk(relaxed = true)
        skjermingGateway = mockk(relaxed = true)
        søknadRepo = mockk(relaxed = true)
        saksoversiktRepo = mockk(relaxed = true)
        val sessionFactory = TestSessionFactory()
        statistikkSakRepo = mockk(relaxed = true)
        statistikkStønadRepo = mockk(relaxed = true)
        behandlingService =
            BehandlingServiceImpl(
                behandlingRepo = behandlingRepo,
                vedtakRepo = vedtakRepo,
                personopplysningRepo = personopplysningRepo,
                brevPublisherGateway = brevPublisherGateway,
                meldekortRepo = meldekortRepo,
                sakRepo = sakRepo,
                sessionFactory = sessionFactory,
                saksoversiktRepo = saksoversiktRepo,
                statistikkSakRepo = statistikkSakRepo,
                statistikkStønadRepo = statistikkStønadRepo,
            )
        sakService =
            SakServiceImpl(
                sakRepo = sakRepo,
                behandlingRepo = behandlingRepo,
                behandlingService = behandlingService,
                personGateway = personGateway,
                skjermingGateway = skjermingGateway,
                sessionFactory = sessionFactory,
                søknadRepo = søknadRepo,
                tiltakGateway = tiltakGateway,
                statistikkSakRepo = statistikkSakRepo,
            )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `sjekk at skjerming blir satt riktig`() {
        val søknad =
            nySøknad(
                tiltak =
                søknadTiltak(
                    deltakelseFom = 1.januar(2023),
                    deltakelseTom = 31.mars(2023),
                ),
            )
        val tiltak = ObjectMother.tiltak()
        val saksbehandler = ObjectMother.saksbehandler(roller = Roller(listOf(SAKSBEHANDLER, SKJERMING)))
        val fnr = søknad.fnr
        val barnFnr = Fnr.random()

        every { sakRepo.hentForIdent(any()) } returns Saker(fnr, emptyList())
        every { sakRepo.lagre(any(), any()) } returnsArgument 0
        every { sakRepo.hentNesteSaksnummer() } returns Saksnummer("202301011001")
        coEvery { tiltakGateway.hentTiltak(any()) } returns listOf(ObjectMother.tiltak())
        every { søknadRepo.hentSøknad(any()) } returns søknad

        coEvery { skjermingGateway.erSkjermetPerson(fnr) } returns true
        coEvery { skjermingGateway.erSkjermetPerson(barnFnr) } returns false
        coEvery { personGateway.hentPerson(any()) } returns
            listOf(
                personopplysningKjedeligFyr(fnr = fnr),
                barn(fnr = barnFnr),
            )
        every { behandlingRepo.lagre(any(), any()) } returnsArgument 0
        every { behandlingRepo.hent(any(), any()) } returns behandlingUnderBehandlingUavklart()
        every { behandlingRepo.hentForSøknadId(any()) } returns null
        coEvery { tiltakGateway.hentTiltak(any()) } returns listOf(tiltak)

        val sak =
            sakService.startFørstegangsbehandling(søknad.id, saksbehandler).getOrElse {
                throw IllegalStateException("Kunne ikke starte førstegangsbehandling + $it")
            }

        sak.personopplysninger.søker().skjermet shouldBe true
        sak.personopplysninger.barnMedIdent(barnFnr)?.skjermet shouldBe false
    }
}
