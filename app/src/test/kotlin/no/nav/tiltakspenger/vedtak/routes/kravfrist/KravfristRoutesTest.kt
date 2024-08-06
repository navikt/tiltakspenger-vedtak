package no.nav.tiltakspenger.vedtak.routes.kravfrist

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
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.clients.meldekort.MeldekortGrunnlagGatewayImpl
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.KravfristVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.kravfristRoutes
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test

internal class KravfristRoutesTest {
    private val mockInnloggetSaksbehandlerProvider = mockk<InnloggetSaksbehandlerProvider>()
    private val mockBrevPublisherGateway = mockk<BrevPublisherGatewayImpl>()
    private val mockMeldekortGrunnlagGateway = mockk<MeldekortGrunnlagGatewayImpl>()

    private val objectMapper: ObjectMapper = defaultObjectMapper()
    private val saksbehandler =
        Saksbehandler(
            "Q123456",
            "Superman",
            "a@b.c",
            listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE),
        )

    @Test
    fun `test at endepunkt for henting av kravfrist fungerer og blir OPPFYLT`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) = testDataHelper.persisterOpprettetFørstegangsbehandling()
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    brevPublisherGateway = mockBrevPublisherGateway,
                    meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
                    sakRepo = testDataHelper.sakRepo,
                    attesteringRepo = testDataHelper.attesteringRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    saksoversiktRepo = testDataHelper.saksoversiktRepo,
                )

            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        kravfristRoutes(
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
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kravfrist")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val kravfristVilkår = objectMapper.readValue<KravfristVilkårDTO>(bodyAsText())
                    kravfristVilkår.samletUtfall shouldBe SamletUtfallDTO.OPPFYLT
                }
            }
        }
    }

    @Test
    fun `test at kravdato gir IKKE_OPPFYLT om det er søkt for lenge etter fristen`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            val (sak, _) =
                testDataHelper.persisterOpprettetFørstegangsbehandling(
                    deltakelseFom = 1.januar(2021),
                    deltakelseTom = 31.januar(2021),
                )
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService =
                BehandlingServiceImpl(
                    behandlingRepo = testDataHelper.behandlingRepo,
                    vedtakRepo = testDataHelper.vedtakRepo,
                    personopplysningRepo = testDataHelper.personopplysningerRepo,
                    brevPublisherGateway = mockBrevPublisherGateway,
                    meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
                    sakRepo = testDataHelper.sakRepo,
                    attesteringRepo = testDataHelper.attesteringRepo,
                    sessionFactory = testDataHelper.sessionFactory,
                    saksoversiktRepo = testDataHelper.saksoversiktRepo,
                )

            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        kravfristRoutes(
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
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kravfrist")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val kravfristVilkår = objectMapper.readValue<KravfristVilkårDTO>(bodyAsText())
                    kravfristVilkår.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT
                }
            }
        }
    }
}
