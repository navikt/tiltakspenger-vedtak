package no.nav.tiltakspenger.vedtak.service

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.service.personopplysning.PersonopplysningService
import no.nav.tiltakspenger.vedtak.service.personopplysning.PersonopplysningServiceImpl
import no.nav.tiltakspenger.vedtak.service.vedtak.VedtakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PersonopplysningServiceTest {
    private lateinit var sakRepo: SakRepo
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var vedtakService: VedtakService
    private lateinit var personopplysningerRepo: PersonopplysningerRepo
    private lateinit var attesteringRepo: AttesteringRepo
    private lateinit var behandlingService: BehandlingService
    private lateinit var personopplysningService: PersonopplysningService

    @BeforeEach
    fun setup() {
        sakRepo = mockk()
        behandlingRepo = mockk()
        vedtakService = mockk()
        personopplysningerRepo = mockk()
        attesteringRepo = mockk()
        behandlingService = BehandlingServiceImpl(behandlingRepo, vedtakService, attesteringRepo, sakRepo)
        personopplysningService = PersonopplysningServiceImpl(personopplysningerRepo, sakRepo)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `motta personopplysninger oppdaterer ikke saksopplysning hvis personopplysninger ikke har endret seg`() {
        val periode = Periode(1.januar(2023), 31.mars(2023))
        val person = ObjectMother.personopplysningKjedeligFyr()
        val sak = ObjectMother.sakMedOpprettetBehandling(
            ident = person.ident,
            personopplysninger = listOf(person),
            periode = periode,
        )
        val personopplysninger = ObjectMother.personopplysningKjedeligFyr(ident = person.ident)

        every { sakRepo.hent(any()) } returns sak
        every { sakRepo.hentSakDetaljerForJournalpostId(any()) } returns sak
        every { sakRepo.hentForJournalpostId(any()) } returns sak
        every { sakRepo.lagre(any()) } returnsArgument 0

        every { behandlingRepo.hent(any()) } returns sak.behandlinger.filterIsInstance<Søknadsbehandling>().first()
        every { behandlingRepo.lagre(any()) } returnsArgument 0
        every { personopplysningerRepo.hent(any()) } returns listOf(personopplysninger)

        personopplysningService.mottaPersonopplysninger(
            "123",
            listOf(person),
        )

        verify(exactly = 0) { sakRepo.lagre(any()) }
    }
}
