package no.nav.tiltakspenger.vedtak.routes.introduksjonsprogrammet

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.every
import io.mockk.mockk
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningFødselsdato
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingServiceImpl
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.clients.meldekort.MeldekortGrunnlagGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakGatewayImpl
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepoImpl
import no.nav.tiltakspenger.vedtak.repository.behandling.KravdatoSaksopplysningRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.SaksopplysningRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.TiltakDAO
import no.nav.tiltakspenger.vedtak.repository.behandling.UtfallsperiodeDAO
import no.nav.tiltakspenger.vedtak.repository.behandling.VurderingRepo
import no.nav.tiltakspenger.vedtak.repository.multi.MultiRepoImpl
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerBarnMedIdentRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerBarnUtenIdentRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresPersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import no.nav.tiltakspenger.vedtak.repository.søknad.BarnetilleggDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadTiltakDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.VedleggDAO
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakRepoImpl
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.IntroVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.introRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.DeltagelseDTO.DELTAR
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.DeltagelseDTO.DELTAR_IKKE
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class IntroRoutesTest {

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    private val mockInnloggetSaksbehandlerProvider = mockk<InnloggetSaksbehandlerProvider>()
    private val mockedUtbetalingServiceImpl = mockk<UtbetalingServiceImpl>()
    private val mockBrevPublisherGateway = mockk<BrevPublisherGatewayImpl>()
    private val mockMeldekortGrunnlagGateway = mockk<MeldekortGrunnlagGatewayImpl>()
    private val mockTiltakGateway = mockk<TiltakGatewayImpl>()

    private val saksopplysningRepo = SaksopplysningRepo()
    private val behandlingRepo = PostgresBehandlingRepo(
        saksopplysningRepo = saksopplysningRepo,
        vurderingRepo = VurderingRepo(),
        søknadDAO = SøknadDAO(
            barnetilleggDAO = BarnetilleggDAO(),
            tiltakDAO = SøknadTiltakDAO(),
            vedleggDAO = VedleggDAO(),
        ),
        tiltakDAO = TiltakDAO(),
        utfallsperiodeDAO = UtfallsperiodeDAO(),
        kravdatoSaksopplysningRepo = KravdatoSaksopplysningRepo(),
        sessionFactory = TestDataHelper(DataSource.hikariDataSource).sessionFactory,
    )

    private val vedtakRepo = VedtakRepoImpl(behandlingRepo = behandlingRepo, utfallsperiodeDAO = UtfallsperiodeDAO())
    private val attesteringDAO = AttesteringRepoImpl()
    private val vedtakRepoImpl = VedtakRepoImpl(behandlingRepo)
    private val multiRepo =
        MultiRepoImpl(behandlingDao = behandlingRepo, attesteringDao = attesteringDAO, vedtakDao = vedtakRepoImpl)

    private val testDataHelper = TestDataHelper(DataSource.hikariDataSource)
    private val personopplysningerRepo = PostgresPersonopplysningerRepo(
        barnMedIdentDAO = PersonopplysningerBarnMedIdentRepo(),
        barnUtenIdentDAO = PersonopplysningerBarnUtenIdentRepo(),
        sessionFactory = testDataHelper.sessionFactory,
    )

    private val sakRepo = PostgresSakRepo(
        behandlingRepo = behandlingRepo,
        personopplysningerRepo = personopplysningerRepo,
        vedtakRepo = vedtakRepo,
        sessionFactory = testDataHelper.sessionFactory,
    )

    private val behandlingService = BehandlingServiceImpl(
        behandlingRepo = behandlingRepo,
        vedtakRepo = vedtakRepo,
        personopplysningRepo = personopplysningerRepo,
        utbetalingService = mockedUtbetalingServiceImpl,
        brevPublisherGateway = mockBrevPublisherGateway,
        meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
        tiltakGateway = mockTiltakGateway,
        multiRepo = multiRepo,
        sakRepo = sakRepo,

    )

    private val objectMapper: ObjectMapper = defaultObjectMapper()
    private val mockSaksbehandler = Saksbehandler(
        "Q123456",
        "Superman",
        "a@b.c",
        listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE),
    )

    @Test
    fun `test at endepunkt for henting og lagring av intro fungerer`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(løpenummer = 1019)

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    introRoutes(
                        innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                        behandlingService = behandlingService,
                    )
                }
            }

            // Sjekk at man kan kjøre Get
            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/introduksjonsprogrammet")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                val introVilkår = objectMapper.readValue<IntroVilkårDTO>(bodyAsText())
                introVilkår.avklartSaksopplysning.periodeMedDeltagelse.deltagelse shouldBe DELTAR_IKKE
            }
        }
    }

    @Test
    fun `test at søknaden blir gjenspeilet i introduksjonsprogrammet vilkåret`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        val sakId = SakId.random()
        val søknadMedIntro = nySøknad(
            intro = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
        )
        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(
            id = sakId,
            behandlinger = listOf(
                Førstegangsbehandling.opprettBehandling(sakId, søknadMedIntro, personopplysningFødselsdato()),
            ),
            løpenummer = 1002,
        )

        sessionOf(DataSource.hikariDataSource).use {
            sakRepo.lagre(objectMotherSak)
        }

        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    introRoutes(
                        innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                        behandlingService = behandlingService,
                    )
                }
            }

            // Sjekk at man kan kjøre Get
            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/$behandlingId/vilkar/introduksjonsprogrammet")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                val introVilkår = objectMapper.readValue<IntroVilkårDTO>(bodyAsText())
                introVilkår.avklartSaksopplysning.periodeMedDeltagelse.deltagelse shouldBe DELTAR
            }
        }
    }
}
