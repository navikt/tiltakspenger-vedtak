package no.nav.tiltakspenger.vedtak.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.domene.vilkår.Utfall
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.sakMedOpprettetBehandling
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.service.sak.SakService
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
import no.nav.tiltakspenger.vedtak.service.vedtak.VedtakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Random

internal class SakServiceTest {
    private lateinit var sakRepo: SakRepo
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var vedtakService: VedtakService
    private lateinit var sakService: SakService
    private lateinit var attesteringRepo: AttesteringRepo

    @BeforeEach
    fun setup() {
        sakRepo = mockk()
        behandlingRepo = mockk()
        vedtakService = mockk()
        attesteringRepo = mockk()
        behandlingService = BehandlingServiceImpl(behandlingRepo, vedtakService, attesteringRepo)
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

        val søknad = nySøknad(
            tiltak = søknadTiltak(
                deltakelseFom = 1.januar(2023),
                deltakelseTom = 31.mars(2023),
            ),
        )

        val sak = sakService.motta(søknad)

        sak.behandlinger.size shouldBe 1
        sak.behandlinger.first() shouldBe beInstanceOf<BehandlingVilkårsvurdert>()

        val behandling = sak.behandlinger.filterIsInstance<BehandlingVilkårsvurdert>().first()
        behandling.vurderingsperiode shouldBe Periode(1.januar(2023), 31.mars(2023))
        behandling.søknader.first() shouldBe søknad
    }

    @Test
    fun `søknad som overlapper med eksisterende sak, legger søknaden til i behandlingen`() {
        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns emptyList()
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

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns listOf(sak)

        val søknad2 = nySøknad(
            journalpostId = "søknad2",
            tiltak = søknadTiltak(
                deltakelseFom = 1.mars(2023),
                deltakelseTom = 31.mars(2023),
            ),
            opprettet = 2.januarDateTime(2023),
        )
        val sak2 = sakService.motta(søknad2)

        sak2.behandlinger.size shouldBe 1
        sak.id shouldBe sak2.id
        sak2.behandlinger.filterIsInstance<BehandlingVilkårsvurdert>().first().søknad() shouldBe søknad2.copy(opprettet = søknad.opprettet)
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

        every { behandlingRepo.hent(any()) } returns sak.behandlinger.filterIsInstance<Søknadsbehandling>().first()
        every { behandlingRepo.lagre(any()) } returnsArgument 0

        sakService.mottaPersonopplysninger(
            "123",
            listOf(
                personopplysningKjedeligFyr(
                    ident = ident,
                    fornavn = "Et endret fornavn",
                ),
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
            personopplysninger = listOf(person),
            periode = periode,
        )
        every { sakRepo.hent(any()) } returns sak
        every { sakRepo.hentForJournalpostId(any()) } returns sak
        every { sakRepo.lagre(any()) } returnsArgument 0

        every { behandlingRepo.hent(any()) } returns sak.behandlinger.filterIsInstance<Søknadsbehandling>().first()
        every { behandlingRepo.lagre(any()) } returnsArgument 0

        sakService.mottaPersonopplysninger(
            "123",
            listOf(person),
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

        every { behandlingRepo.hent(any()) } returns sak.behandlinger.filterIsInstance<Søknadsbehandling>().first()
        every { behandlingRepo.lagre(any()) } returnsArgument 0

        sakService.mottaPersonopplysninger(
            journalpostId = "123",
            personopplysninger = listOf(
                personopplysningKjedeligFyr(
                    ident = ident,
                    fødselsdato = 31.januar(2023).minusYears(18),
                ),
            ),
        )

        verify {
            behandlingRepo.lagre(
                match { behandling ->
                    behandling.saksopplysninger.first { it.vilkår == Vilkår.ALDER }.fom == 1.januar(2023) &&
                        behandling.saksopplysninger.first { it.vilkår == Vilkår.ALDER }.tom == 30.januar(2023) &&
                        behandling.saksopplysninger.first { it.vilkår == Vilkår.ALDER }.typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE &&
                        (behandling as BehandlingVilkårsvurdert).vilkårsvurderinger.filter { it.vilkår == Vilkår.ALDER }
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
