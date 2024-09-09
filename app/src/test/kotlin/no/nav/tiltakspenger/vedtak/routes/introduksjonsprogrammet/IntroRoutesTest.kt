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
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.felles.service.AuditService
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.Roller
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.IntroVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.introRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.DeltagelseDTO.DELTAR
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.DeltagelseDTO.DELTAR_IKKE
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IntroRoutesTest {
    private val mockInnloggetSaksbehandlerProvider = mockk<InnloggetSaksbehandlerProvider>()
    private val mockBrevPublisherGateway = mockk<BrevPublisherGatewayImpl>()
    private val mockAuditService = mockk<AuditService>()

    private val objectMapper: ObjectMapper = defaultObjectMapper()
    private val mockSaksbehandler =
        Saksbehandler(
            "Q123456",
            "Superman",
            "a@b.c",
            Roller(listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE)),
        )

    @BeforeEach
    fun setup() {
        every { mockAuditService.logMedBehandlingId(any(), any(), any(), any()) } returns Unit
    }

    @Test
    fun `test at endepunkt for henting og lagring av intro fungerer`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) =
                testDataHelper.persisterOpprettetFørstegangsbehandling(
                    deltakelseFom = 1.januar(2023),
                    deltakelseTom = 31.mars(2023),
                )
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    brevPublisherGateway = mockBrevPublisherGateway,
                    sakRepo = testDataHelper.sakRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    saksoversiktRepo = testDataHelper.saksoversiktRepo,
                    statistikkSakRepo = testDataHelper.statistikkSakRepo,
                    statistikkStønadRepo = testDataHelper.statistikkStønadRepo,
                    meldekortRepo = testDataHelper.meldekortRepo,
                )

            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        introRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            behandlingService = behandlingService,
                            auditService = mockAuditService,
                        )
                    }
                }

                // Sjekk at man kan kjøre Get
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/introduksjonsprogrammet")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val introVilkår = objectMapper.readValue<IntroVilkårDTO>(bodyAsText())
                    introVilkår.avklartSaksopplysning.periodeMedDeltagelse.deltagelse shouldBe DELTAR_IKKE
                }
            }
        }
    }

    @Test
    fun `test at søknaden blir gjenspeilet i introduksjonsprogrammet vilkåret`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns mockSaksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val søknadMedIntro =
                nySøknad(
                    intro = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
                )

            val (sak, _) =
                testDataHelper.persisterOpprettetFørstegangsbehandling(
                    søknad = søknadMedIntro,
                )
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    brevPublisherGateway = mockBrevPublisherGateway,
                    sakRepo = testDataHelper.sakRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    saksoversiktRepo = testDataHelper.saksoversiktRepo,
                    statistikkSakRepo = testDataHelper.statistikkSakRepo,
                    statistikkStønadRepo = testDataHelper.statistikkStønadRepo,
                    meldekortRepo = testDataHelper.meldekortRepo,
                )

            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        introRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            behandlingService = behandlingService,
                            auditService = mockAuditService,
                        )
                    }
                }

                // Sjekk at man kan kjøre Get
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/introduksjonsprogrammet")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val introVilkår = objectMapper.readValue<IntroVilkårDTO>(bodyAsText())
                    introVilkår.avklartSaksopplysning.periodeMedDeltagelse.deltagelse shouldBe DELTAR
                }
            }
        }
    }
}
