package no.nav.tiltakspenger.saksbehandling.service

import arrow.core.getOrElse
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.TestSessionFactory
import no.nav.tiltakspenger.felles.Rolle.SAKSBEHANDLER
import no.nav.tiltakspenger.felles.Rolle.SKJERMING
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.random
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
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.vedtak.VedtakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SakServiceTest {
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var vedtakRepo: VedtakRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var vedtakService: VedtakService
    private lateinit var brevPublisherGateway: BrevPublisherGateway
    private lateinit var meldekortGrunnlagGateway: MeldekortGrunnlagGateway
    private lateinit var tiltakGateway: TiltakGateway
    private lateinit var personopplysningRepo: PersonopplysningerRepo
    private lateinit var sakRepo: SakRepo
    private lateinit var sakService: SakService
    private lateinit var personGateway: PersonGateway
    private lateinit var skjermingGateway: SkjermingGateway
    private lateinit var søknadRepo: SøknadRepo
    private lateinit var saksoversiktRepo: SaksoversiktRepo

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk()
        vedtakRepo = mockk()
        vedtakService = mockk()
        brevPublisherGateway = mockk()
        meldekortGrunnlagGateway = mockk()
        tiltakGateway = mockk()
        sakRepo = mockk()
        personopplysningRepo = mockk(relaxed = true)
        personGateway = mockk(relaxed = true)
        skjermingGateway = mockk(relaxed = true)
        søknadRepo = mockk(relaxed = true)
        saksoversiktRepo = mockk(relaxed = true)
        val sessionFactory = TestSessionFactory()
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
            )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    // TODO jah: Vi skal ikke støtte mer enn en sak i første omgang. Dessuten hadde det vært bedre å gjøre den om til en integrasjonstest ved å teste på route-nivå
//    @Test
//    fun `søknad som ikke overlapper med eksisterende sak, blir en ny sak med en behandling`() {
//        val søknad = nySøknad(
//            tiltak = søknadTiltak(
//                deltakelseFom = 1.januar(2023),
//                deltakelseTom = 31.mars(2023),
//            ),
//        )
//        val saksbehandler = ObjectMother.saksbehandler()
//        val ident = søknad.personopplysninger.ident
//        every { sakRepo.hentForIdent(any()) } returns Saker(ident, emptyList())
//        every { sakRepo.lagre(any(), any()) } returnsArgument 0
//        every { sakRepo.hentNesteSaksnummer() } returns Saksnummer("202301011001")
//        every { søknadRepo.hentSøknad(any()) } returns søknad
//
//        coEvery { tiltakGateway.hentTiltak(any()) } returns listOf(ObjectMother.tiltak())

    // coEvery { personGateway.hentPerson(any()) } returns listOf(personopplysningKjedeligFyr(ident = ident))
//        every { behandlingRepo.hent(any(),any()) } returns behandling()
//        every { behandlingRepo.hentForSøknadId(any()) } returns behandling()
//        every { behandlingRepo.lagre(any()) } returnsArgument 0
//
//        val sak =
//            sakService.startFørstegangsbehandling(søknadId = søknad.id, saksbehandler = saksbehandler).getOrElse {
//                throw IllegalStateException("Kunne ikke starte førstegangsbehandling + $it")
//
//            }
//
//        sak.behandlinger.size shouldBe 1
//        sak.behandlinger.first().tilstand shouldBe BehandlingTilstand.OPPRETTET
//
//        val behandling = sak.behandlinger.first { it.tilstand == BehandlingTilstand.OPPRETTET }
//        behandling.vurderingsperiode shouldBe Periode(1.januar(2023), 31.mars(2023))
//        behandling.søknader.first() shouldBe søknad
//    }

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
        val saksbehandler = ObjectMother.saksbehandler(roller = listOf(SAKSBEHANDLER, SKJERMING))
        val fnr = søknad.personopplysninger.fnr
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
