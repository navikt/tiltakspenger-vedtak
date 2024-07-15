package no.nav.tiltakspenger.saksbehandling.service

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother.barn
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandling
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.MultiRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.ports.SøkerRepository
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.statistikk.StatistikkService
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingService
import no.nav.tiltakspenger.saksbehandling.service.vedtak.VedtakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class SakServiceTest {
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var vedtakRepo: VedtakRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var vedtakService: VedtakService
    private lateinit var utbetalingService: UtbetalingService
    private lateinit var brevPublisherGateway: BrevPublisherGateway
    private lateinit var meldekortGrunnlagGateway: MeldekortGrunnlagGateway
    private lateinit var tiltakGateway: TiltakGateway
    private lateinit var multiRepo: MultiRepo
    private lateinit var personopplysningRepo: PersonopplysningerRepo
    private lateinit var sakRepo: SakRepo
    private lateinit var sakService: SakService
    private lateinit var personGateway: PersonGateway
    private lateinit var skjermingGateway: SkjermingGateway
    private lateinit var søkerRepository: SøkerRepository
    private lateinit var statistikkService: StatistikkService

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk()
        vedtakRepo = mockk()
        vedtakService = mockk()
        utbetalingService = mockk()
        brevPublisherGateway = mockk()
        meldekortGrunnlagGateway = mockk()
        tiltakGateway = mockk()
        multiRepo = mockk()
        sakRepo = mockk()
        personopplysningRepo = mockk(relaxed = true)
        personGateway = mockk(relaxed = true)
        skjermingGateway = mockk(relaxed = true)
        søkerRepository = mockk(relaxed = true)
        statistikkService = mockk(relaxed = true)
        behandlingService =
            BehandlingServiceImpl(
                behandlingRepo = behandlingRepo,
                vedtakRepo = vedtakRepo,
                personopplysningRepo = personopplysningRepo,
                utbetalingService = utbetalingService,
                statistikkService = statistikkService,
                brevPublisherGateway = brevPublisherGateway,
                meldekortGrunnlagGateway = meldekortGrunnlagGateway,
                tiltakGateway = tiltakGateway,
                multiRepo = multiRepo,
                sakRepo = sakRepo,
            )
        sakService = SakServiceImpl(
            sakRepo = sakRepo,
            behandlingRepo = behandlingRepo,
            søkerRepository = søkerRepository,
            behandlingService = behandlingService,
            personGateway = personGateway,
            skjermingGateway = skjermingGateway,
            statistikkService = statistikkService,
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `søknad som ikke overlapper med eksisterende sak, blir en ny sak med en behandling`() {
        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns emptyList()
        every { sakRepo.lagre(any()) } returnsArgument 0
        every { sakRepo.hentNesteLøpenr() } returns "1"

        val søknad = nySøknad(
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.mars(2023),
            ),
        )
        val ident = søknad.personopplysninger.ident
        coEvery { personGateway.hentPerson(any()) } returns listOf(personopplysningKjedeligFyr(ident = ident))
        every { behandlingRepo.hent(any()) } returns behandling()
        every { behandlingRepo.lagre(any()) } returnsArgument 0

        val sak = sakService.motta(søknad)

        sak.behandlinger.size shouldBe 1
        sak.behandlinger.first().tilstand shouldBe BehandlingTilstand.VILKÅRSVURDERT

        val behandling = sak.behandlinger.first { it.tilstand == BehandlingTilstand.VILKÅRSVURDERT }
        behandling.vurderingsperiode shouldBe Periode(1.januar(2023), 31.mars(2023))
        behandling.søknader.first() shouldBe søknad
    }

    @Test
    fun `sjekk at skjerming blir satt riktig`() {
        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns emptyList()
        every { sakRepo.lagre(any()) } returnsArgument 0
        every { sakRepo.hentNesteLøpenr() } returns "1"

        val søknad = nySøknad(
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.mars(2023),
            ),
        )
        val ident = søknad.personopplysninger.ident
        val barnIdent = "barnIdent"
        coEvery { skjermingGateway.erSkjermetPerson(ident) } returns true
        coEvery { skjermingGateway.erSkjermetPerson(barnIdent) } returns false
        coEvery { personGateway.hentPerson(any()) } returns listOf(personopplysningKjedeligFyr(ident = ident), barn(ident = barnIdent))
        every { behandlingRepo.lagre(any()) } returnsArgument 0
        every { behandlingRepo.hent(any()) } returns behandling()

        val sak = sakService.motta(søknad)

        sak.personopplysninger.søker().skjermet shouldBe true
        sak.personopplysninger.barnMedIdent(barnIdent)?.skjermet shouldBe false
    }

    // TODO jah: Ser ikke ut som vi oppdaterer vurderingsperioden når vi legger til en overlappende søknad? Og dersom vi ikke har overlappene søknader, så får vi ikke forskjellige behandlinger? Ser på dette etter vilkår2
    @Disabled("kew: Disabler testen siden vi ikke skal tenke på 2 søknader per nå.")
    @Test
    fun `søknad som overlapper med eksisterende sak, legger søknaden til i behandlingen`() {
        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns emptyList()
        every { sakRepo.lagre(any()) } returnsArgument 0
        every { sakRepo.hentNesteLøpenr() } returns "1"

        val søknad = nySøknad(
            journalpostId = "søknad1",
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.januar(2023),
            ),
            opprettet = 1.januarDateTime(2023),
        )
        val ident = søknad.personopplysninger.ident
        coEvery { personGateway.hentPerson(any()) } returns listOf(personopplysningKjedeligFyr(ident = ident))
        every { behandlingRepo.lagre(any()) } returnsArgument 0
        val sak = sakService.motta(søknad)

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(sak)

        val søknad2 = nySøknad(
            journalpostId = "søknad2",
            tiltak = søknadTiltak(
                // TODO jah: Mars overlapper vel ikke med januar? Hva skjer med testnavnet her. Og hvorfor forventer vi at behandlinger size er 1?
                deltakelseFom = 1.mars(2023),
                deltakelseTom = 31.mars(2023),
            ),
            opprettet = 2.januarDateTime(2023),
        )
        val sak2 = sakService.motta(søknad2)

        sak2.behandlinger.size shouldBe 1
        sak.id shouldBe sak2.id
        sak2.behandlinger.first { it.tilstand == BehandlingTilstand.VILKÅRSVURDERT }
            .søknad() shouldBe søknad2.copy(opprettet = søknad.opprettet)
    }

//    @Test
//    fun `legger til ny søknad med flere iverksatte, lager 1 ny behandling`() {
//        val eksisterendeSak = tomSak(
//            periode = Periode(
//                1.januar(2023),
//                31.mars(2023),
//            ),
//            behandlinger = listOf(
//                behandlingInnvilgetIverksatt(),
//                behandlingInnvilgetIverksatt(),
//            ),
//        )
//
//        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(eksisterendeSak)
//        every { sakRepo.lagre(any()) } returnsArgument 0
//
//        val søknad = nySøknad(
//            journalpostId = "søknad1",
//            tiltak = søknadTiltak(
//                deltakelseFom = 1.januar(2023),
//                deltakelseTom = 31.januar(2023),
//            ),
//            opprettet = 1.januarDateTime(2023),
//        )
//        coEvery { personGateway.hentPerson(any()) } returns eksisterendeSak.personopplysninger.liste
//        every { behandlingRepo.lagre(any()) } returnsArgument 0
//        val sak = sakService.motta(søknad)
//
//        sak.behandlinger.size shouldBe 3
//    }

//    @Test
//    fun `legger til ny søknad med flere iverksatte og 1 til beslutter, legger søknad til den åpne behandlingen`() {
//        val eksisterendeSak = tomSak(
//            periode = Periode(
//                1.januar(2023),
//                31.mars(2023),
//            ),
//            behandlinger = listOf(
//                behandlingInnvilgetIverksatt(),
//                behandlingInnvilgetIverksatt(),
//                behandlingTilBeslutterInnvilget(),
//            ),
//        )
//
//        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(eksisterendeSak)
//        every { sakRepo.lagre(any()) } returnsArgument 0
//
//        val nyJournalpostId = "ny og fin journalpostid"
//        val søknad = nySøknad(
//            journalpostId = nyJournalpostId,
//            tiltak = søknadTiltak(
//                deltakelseFom = 1.januar(2023),
//                deltakelseTom = 31.januar(2023),
//            ),
//            opprettet = 1.januarDateTime(2023),
//        )
//        val ident = søknad.personopplysninger.ident
//        coEvery { personGateway.hentPerson(any()) } returns eksisterendeSak.personopplysninger.liste
//        every { behandlingRepo.lagre(any()) } returnsArgument 0
//
//        val sak = sakService.motta(søknad)
//
//        sak.behandlinger.size shouldBe 3
//
//        val åpenBehandlinger = sak.behandlinger.filterNot { it.tilstand == BehandlingTilstand.IVERKSATT }
//
//        åpenBehandlinger.size shouldBe 1
//        val b = åpenBehandlinger.first { it.tilstand == BehandlingTilstand.VILKÅRSVURDERT }
//        b.søknader.size shouldBe 2
//        b.søknad().journalpostId shouldBe nyJournalpostId
//    }
}
