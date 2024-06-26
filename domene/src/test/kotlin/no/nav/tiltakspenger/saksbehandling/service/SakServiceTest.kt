package no.nav.tiltakspenger.saksbehandling.service

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingInnvilgetIverksatt
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.sakMedOpprettetBehandling
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.tomSak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
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
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingService
import no.nav.tiltakspenger.saksbehandling.service.vedtak.VedtakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Random

internal class SakServiceTest {
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var vedtakRepo: VedtakRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var vedtakService: VedtakService
    private lateinit var utbetalingService: UtbetalingService
    private lateinit var brevPublisherGateway: BrevPublisherGateway
    private lateinit var meldekortGrunnlagGateway: MeldekortGrunnlagGateway
    private lateinit var multiRepo: MultiRepo
    private lateinit var personopplysningRepo: PersonopplysningerRepo
    private lateinit var sakRepo: SakRepo
    private lateinit var sakService: SakService

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk()
        vedtakRepo = mockk()
        vedtakService = mockk()
        utbetalingService = mockk()
        brevPublisherGateway = mockk()
        meldekortGrunnlagGateway = mockk()
        multiRepo = mockk()
        sakRepo = mockk()
        personopplysningRepo = mockk(relaxed = true)
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
        sakService = SakServiceImpl(sakRepo, behandlingRepo, behandlingService)
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

        val sak = sakService.motta(søknad)

        sak.behandlinger.size shouldBe 1
        sak.behandlinger.first().tilstand shouldBe BehandlingTilstand.VILKÅRSVURDERT

        val behandling = sak.behandlinger.first { it.tilstand == BehandlingTilstand.VILKÅRSVURDERT }
        behandling.vurderingsperiode shouldBe Periode(1.januar(2023), 31.mars(2023))
        behandling.søknader.first() shouldBe søknad
    }

    // TODO jah: Ser ikke ut som vi oppdaterer vurderingsperioden når vi legger til en overlappende søknad? Og dersom vi ikke har overlappene søknader, så får vi ikke forskjellige behandlinger? Ser på dette etter vilkår2
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

    @Test
    fun `legger til ny søknad med flere iverksatte, lager 1 ny behandling`() {
        val eksisterendeSak = tomSak(
            periode = Periode(
                1.januar(2023),
                31.mars(2023),
            ),
            behandlinger = listOf(
                behandlingInnvilgetIverksatt(),
                behandlingInnvilgetIverksatt(),
            ),
        )

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(eksisterendeSak)
        every { sakRepo.lagre(any()) } returnsArgument 0

        val søknad = nySøknad(
            journalpostId = "søknad1",
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.januar(2023),
            ),
            opprettet = 1.januarDateTime(2023),
        )

        val sak = sakService.motta(søknad)

        sak.behandlinger.size shouldBe 3
    }

    @Test
    fun `legger til ny søknad med flere iverksatte og 1 til beslutter, legger søknad til den åpne behandlingen`() {
        val eksisterendeSak = tomSak(
            periode = Periode(
                1.januar(2023),
                31.mars(2023),
            ),
            behandlinger = listOf(
                behandlingInnvilgetIverksatt(),
                behandlingInnvilgetIverksatt(),
                behandlingTilBeslutterInnvilget(),
            ),
        )

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(eksisterendeSak)
        every { sakRepo.lagre(any()) } returnsArgument 0

        val nyJournalpostId = "ny og fin journalpostid"
        val søknad = nySøknad(
            journalpostId = nyJournalpostId,
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.januar(2023),
            ),
            opprettet = 1.januarDateTime(2023),
        )

        val sak = sakService.motta(søknad)

        sak.behandlinger.size shouldBe 3

        val åpenBehandlinger = sak.behandlinger.filterNot { it.tilstand == BehandlingTilstand.IVERKSATT }

        åpenBehandlinger.size shouldBe 1
        val b = åpenBehandlinger.first { it.tilstand == BehandlingTilstand.VILKÅRSVURDERT }
        b.søknader.size shouldBe 2
        b.søknad().journalpostId shouldBe nyJournalpostId
    }

    @Test
    fun `motta personopplysninger oppdaterer saksopplysning for ALDER hvis det er en endring`() {
        val periode = Periode(1.januar(2023), 31.mars(2023))
        val ident = Random().nextInt().toString()
        val sak = sakMedOpprettetBehandling(
            ident = ident,
            periode = periode,
        )
        every { sakRepo.hent(any()) } returns sak
        every { sakRepo.hentForJournalpostId(any()) } returns sak
        every { sakRepo.lagre(any()) } returnsArgument 0

        every { behandlingRepo.hent(any()) } returns sak.behandlinger.filterIsInstance<Førstegangsbehandling>()
            .first()
        every { behandlingRepo.lagre(any()) } returnsArgument 0

        sakService.mottaPersonopplysninger(
            "123",
            SakPersonopplysninger(
                listOf(personopplysningKjedeligFyr(ident = ident, fornavn = "Et endret fornavn")),
            ),
        )

        verify {
            behandlingRepo.lagre(
                match { behandling ->
                    behandling.saksopplysninger.first { it.vilkår == Vilkår.ALDER }.fom == 1.januar(2023) &&
                        behandling.saksopplysninger.first { it.vilkår == Vilkår.ALDER }.tom == 31.mars(2023) &&
                        behandling.saksopplysninger.first { it.vilkår == Vilkår.ALDER }.typeSaksopplysning == TypeSaksopplysning.HAR_IKKE_YTELSE
                },
            )
        }
    }

    @Test
    fun `motta personopplysninger oppdaterer ikke saksopplysning hvis personopplysninger ikke har endret seg`() {
        val periode = Periode(1.januar(2023), 31.mars(2023))
        val person = personopplysningKjedeligFyr()
        val sak = sakMedOpprettetBehandling(
            ident = person.ident,
            personopplysninger = SakPersonopplysninger(listOf(person)),
            periode = periode,
        )
        every { sakRepo.hent(any()) } returns sak
        every { sakRepo.hentForJournalpostId(any()) } returns sak
        every { sakRepo.lagre(any()) } returnsArgument 0

        every { behandlingRepo.hent(any()) } returns sak.behandlinger.filterIsInstance<Førstegangsbehandling>()
            .first()
        every { behandlingRepo.lagre(any()) } returnsArgument 0

        sakService.mottaPersonopplysninger(
            "123",
            SakPersonopplysninger(listOf(person)),
        )

        verify(exactly = 0) { sakRepo.lagre(any()) }
    }

    @Test
    fun `motta personopplysninger for en person som blir 18 midt i perioden`() {
        val periode = Periode(1.januar(2023), 31.mars(2023))
        val ident = Random().nextInt().toString()
        val sak = sakMedOpprettetBehandling(
            ident = ident,
            periode = periode,
        )
        every { sakRepo.hent(any()) } returns sak
        every { sakRepo.hentForJournalpostId(any()) } returns sak
        every { sakRepo.lagre(any()) } returnsArgument 0

        every { behandlingRepo.hent(any()) } returns sak.behandlinger.filterIsInstance<Førstegangsbehandling>()
            .first()
        every { behandlingRepo.lagre(any()) } returnsArgument 0

        sakService.mottaPersonopplysninger(
            journalpostId = "123",
            nyePersonopplysninger = SakPersonopplysninger(
                listOf(
                    personopplysningKjedeligFyr(
                        ident = ident,
                        fødselsdato = 31.januar(2023).minusYears(18),
                    ),
                ),
            ),
        )

        verify {
            behandlingRepo.lagre(
                match { behandling ->
                    behandling.saksopplysninger.first { it.vilkår == Vilkår.ALDER }.fom == 1.januar(2023) &&
                        behandling.saksopplysninger.first { it.vilkår == Vilkår.ALDER }.tom == 30.januar(2023) &&
                        behandling.saksopplysninger.first { it.vilkår == Vilkår.ALDER }.typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE &&
                        behandling.vilkårsvurderinger.filter { it.vilkår == Vilkår.ALDER }
                            .sortedBy { it.fom }.first().fom == 1.januar(2023) &&
                        behandling.vilkårsvurderinger.filter { it.vilkår == Vilkår.ALDER }
                            .sortedBy { it.fom }.first().tom == 30.januar(2023) &&
                        behandling.vilkårsvurderinger.filter { it.vilkår == Vilkår.ALDER }
                            .sortedBy { it.fom }.first().utfall == Utfall.IKKE_OPPFYLT &&
                        behandling.vilkårsvurderinger.filter { it.vilkår == Vilkår.ALDER }
                            .sortedBy { it.fom }.last().fom == 31.januar(2023) &&
                        behandling.vilkårsvurderinger.filter { it.vilkår == Vilkår.ALDER }
                            .sortedBy { it.fom }.last().tom == 31.mars(2023) &&
                        behandling.vilkårsvurderinger.filter { it.vilkår == Vilkår.ALDER }
                            .sortedBy { it.fom }.last().utfall == Utfall.OPPFYLT
                },

            )
        }
    }
}
