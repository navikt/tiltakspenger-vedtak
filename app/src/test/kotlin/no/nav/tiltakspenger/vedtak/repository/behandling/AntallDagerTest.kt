package no.nav.tiltakspenger.vedtak.repository.behandling

import io.kotest.inspectors.forAll
import io.kotest.inspectors.forAtLeastOne
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.Tiltak
import no.nav.tiltakspenger.saksbehandling.ports.AttesteringRepo
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AntallDagerTest {

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

    private val saksbehandlerMedTilgang = ObjectMother.saksbehandler()

    private val vurderingFom = LocalDate.of(2023, 1, 1)
    private val vurderingTom = LocalDate.of(2023, 6, 1)
    private val vurderingsPeriode = Periode(
        fraOgMed = vurderingFom,
        tilOgMed = vurderingTom,
    )

    private val oppdatertAntallDagerVerdi = 1

    private val tiltak = Tiltak(
        id = TiltakId.random(),
        eksternId = "123",
        gjennomføring = Tiltak.Gjennomføring(
            id = "123",
            arrangørnavn = "arrangør",
            typeNavn = "Jobbkurs",
            typeKode = "JOBBK",
            rettPåTiltakspenger = true,
        ),
        deltakelseFom = vurderingFom,
        deltakelseTom = vurderingTom,
        deltakelseStatus = Tiltak.DeltakerStatus(status = "DELTAR", rettTilÅSøke = true),
        deltakelseProsent = 100.0F,
        kilde = "Komet",
        registrertDato = 1.januarDateTime(2023),
        innhentet = 1.januarDateTime(2023),
        antallDagerSaksopplysninger = AntallDagerSaksopplysninger.initAntallDagerSaksopplysning(
            antallDager = listOf(
                PeriodeMedVerdi(
                    verdi = AntallDager(antallDager = 2, kilde = Kilde.ARENA, saksbehandlerIdent = null),
                    periode = vurderingsPeriode,
                ),
            ),
            avklarteAntallDager = emptyList(),
        ),
    )

    @BeforeEach
    fun setup() {
        behandlingRepo = mockk()
        vedtakRepo = mockk()
        personopplysningRepo = mockk(relaxed = true)
        utbetalingService = mockk()
        brevPublisherGateway = mockk()
        meldekortGrunnlagGateway = mockk()
        attesteringRepo = mockk(relaxed = true)
        sakRepo = mockk(relaxed = true)
        tiltakGateway = mockk(relaxed = true)

        behandlingService = BehandlingServiceImpl(
            behandlingRepo = behandlingRepo,
            vedtakRepo = vedtakRepo,
            personopplysningRepo = personopplysningRepo,
            utbetalingService = utbetalingService,
            brevPublisherGateway = brevPublisherGateway,
            meldekortGrunnlagGateway = meldekortGrunnlagGateway,
            tiltakGateway = tiltakGateway,
            sakRepo = sakRepo,
            attesteringRepo = attesteringRepo,
            sessionFactory = mockk(),
        )
    }

    @Test
    fun `sjekk at saksbehandler kan legge til antall dager og at de blir avklart`() {
        val behandling = ObjectMother.behandling(periode = vurderingsPeriode).startBehandling(saksbehandlerMedTilgang)
            .vilkårsvurder()
        val behandlingId = behandling.id

        val lagretBehandling = slot<Førstegangsbehandling>()
        val oppdatertBehandling = behandling.oppdaterTiltak(listOf(tiltak))

        every { behandlingRepo.hent(any()) } returns oppdatertBehandling
        every { behandlingRepo.lagre(capture(lagretBehandling)) } returnsArgument 0

        behandlingRepo.lagre(oppdatertBehandling)

        lagretBehandling.captured.tiltak.tiltak shouldHaveSize 1
        val tiltakId = lagretBehandling.captured.tiltak.tiltak.first().id

        val saksbehandlerPeriode = Periode(
            fraOgMed = vurderingsPeriode.fraOgMed,
            tilOgMed = vurderingsPeriode.tilOgMed.minusDays(1),
        )

        val periodeMedAntallDager = PeriodeMedVerdi(
            verdi = AntallDager(
                antallDager = oppdatertAntallDagerVerdi,
                kilde = Kilde.SAKSB,
                saksbehandlerIdent = saksbehandlerMedTilgang.navIdent,
            ),
            periode = saksbehandlerPeriode,
        )

        behandlingService.oppdaterAntallDagerPåTiltak(
            behandlingId = behandlingId,
            tiltakId = tiltakId,
            periodeMedAntallDager = periodeMedAntallDager,
            saksbehandler = saksbehandlerMedTilgang,
        )

        val antallDagerSaksopplysning = lagretBehandling.captured.tiltak.tiltak.first().antallDagerSaksopplysninger

        antallDagerSaksopplysning.antallDagerSaksopplysningerFraSBH shouldHaveSize 2
        antallDagerSaksopplysning.antallDagerSaksopplysningerFraSBH.forAll { it.verdi.kilde shouldBe Kilde.SAKSB }
        antallDagerSaksopplysning.antallDagerSaksopplysningerFraSBH.forAtLeastOne {
            it.periode shouldBe saksbehandlerPeriode
            it.verdi.antallDager shouldBe oppdatertAntallDagerVerdi
            it.verdi.saksbehandlerIdent shouldBe saksbehandlerMedTilgang.navIdent
            it.verdi.kilde shouldBe Kilde.SAKSB
        }

        antallDagerSaksopplysning.avklartAntallDager.shouldContainAll(antallDagerSaksopplysning.antallDagerSaksopplysningerFraSBH)
    }

    @Test
    fun `sjekk at info fra saksbehandler blir fjernet når man tilbakestiller og at data fra register blir avklart`() {
        val behandling = ObjectMother.behandling(periode = vurderingsPeriode).startBehandling(saksbehandlerMedTilgang)
            .vilkårsvurder()
        val behandlingId = behandling.id

        val lagretBehandling = slot<Førstegangsbehandling>()
        val oppdatertBehandling = behandling.oppdaterTiltak(listOf(tiltak))

        every { behandlingRepo.hent(any()) } returns oppdatertBehandling
        every { behandlingRepo.lagre(capture(lagretBehandling)) } returnsArgument 0

        behandlingRepo.lagre(oppdatertBehandling)

        lagretBehandling.captured.tiltak.tiltak shouldHaveSize 1
        val tiltakId = lagretBehandling.captured.tiltak.tiltak.first().id

        val saksbehandlerPeriode = Periode(
            fraOgMed = vurderingsPeriode.fraOgMed,
            tilOgMed = vurderingsPeriode.tilOgMed.minusDays(1),
        )

        val periodeMedAntallDager = PeriodeMedVerdi(
            verdi = AntallDager(
                antallDager = oppdatertAntallDagerVerdi,
                kilde = Kilde.SAKSB,
                saksbehandlerIdent = saksbehandlerMedTilgang.navIdent,
            ),
            periode = saksbehandlerPeriode,
        )

        behandlingService.oppdaterAntallDagerPåTiltak(
            behandlingId = behandlingId,
            tiltakId = tiltakId,
            periodeMedAntallDager = periodeMedAntallDager,
            saksbehandler = saksbehandlerMedTilgang,
        )

        val antallDagerSaksopplysning = lagretBehandling.captured.tiltak.tiltak.first().antallDagerSaksopplysninger

        antallDagerSaksopplysning.antallDagerSaksopplysningerFraSBH shouldHaveSize 2

        behandlingService.tilbakestillAntallDagerPåTiltak(
            behandlingId = oppdatertBehandling.id,
            tiltakId = oppdatertBehandling.tiltak.tiltak.first().id,
            saksbehandler = saksbehandlerMedTilgang,
        )

        val antallDagerEtterTilbakestilling =
            lagretBehandling.captured.tiltak.tiltak.first().antallDagerSaksopplysninger

        antallDagerEtterTilbakestilling.antallDagerSaksopplysningerFraSBH shouldHaveSize 0
        antallDagerEtterTilbakestilling.avklartAntallDager
            .shouldContainAll(antallDagerEtterTilbakestilling.antallDagerSaksopplysningerFraRegister)
    }
}
