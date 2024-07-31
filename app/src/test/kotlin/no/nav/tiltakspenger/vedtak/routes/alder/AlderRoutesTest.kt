package no.nav.tiltakspenger.vedtak.routes.alder

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
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingServiceImpl
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.clients.meldekort.MeldekortGrunnlagGatewayImpl
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.alder.AlderVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.alder.alderRoutes
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AlderRoutesTest {

    private val mockInnloggetSaksbehandlerProvider = mockk<InnloggetSaksbehandlerProvider>()
    private val mockedUtbetalingServiceImpl = mockk<UtbetalingServiceImpl>()
    private val mockBrevPublisherGateway = mockk<BrevPublisherGatewayImpl>()
    private val mockMeldekortGrunnlagGateway = mockk<MeldekortGrunnlagGatewayImpl>()

    private val objectMapper: ObjectMapper = defaultObjectMapper()
    private val saksbehandler = Saksbehandler(
        "Q123456",
        "Superman",
        "a@b.c",
        listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE),
    )

    @Test
    fun `test at endepunkt for henting og lagring av alder fungerer`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        val objectMotherSak = ObjectMother.sakMedOpprettetBehandling(løpenummer = 1021)
        val behandlingId = objectMotherSak.behandlinger.first().id.toString()

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)

            testDataHelper.sessionFactory.withTransaction {
                testDataHelper.sakRepo.lagre(objectMotherSak)
            }

            val behandlingService = BehandlingServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                vedtakRepo = testDataHelper.vedtakRepo,
                personopplysningRepo = testDataHelper.personopplysningerRepo,
                utbetalingService = mockedUtbetalingServiceImpl,
                brevPublisherGateway = mockBrevPublisherGateway,
                meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
                sakRepo = testDataHelper.sakRepo,
                attesteringRepo = testDataHelper.attesteringRepo,
                sessionFactory = testDataHelper.sessionFactory,
                søknadRepo = testDataHelper.søknadRepo,
            )

            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        alderRoutes(
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
                        path("$behandlingPath/$behandlingId/vilkar/alder")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val alderVilkår = objectMapper.readValue<AlderVilkårDTO>(bodyAsText())
                    alderVilkår.samletUtfall shouldBe SamletUtfallDTO.OPPFYLT
                }
            }
        }
    }

    @Test
    fun `test at søknaden blir gjenspeilet i alder vilkåret`() {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val (sak, _) = testDataHelper.persisterOpprettetFørstegangsbehandling(
                fødselsdato = LocalDate.now().minusYears(10),
            )
            val behandlingId = sak.førstegangsbehandling.id

            val behandlingService = BehandlingServiceImpl(
                behandlingRepo = testDataHelper.behandlingRepo,
                vedtakRepo = testDataHelper.vedtakRepo,
                personopplysningRepo = testDataHelper.personopplysningerRepo,
                utbetalingService = mockedUtbetalingServiceImpl,
                brevPublisherGateway = mockBrevPublisherGateway,
                meldekortGrunnlagGateway = mockMeldekortGrunnlagGateway,
                sakRepo = testDataHelper.sakRepo,
                attesteringRepo = testDataHelper.attesteringRepo,
                sessionFactory = testDataHelper.sessionFactory,
                søknadRepo = testDataHelper.søknadRepo,
            )

            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        alderRoutes(
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
                        path("$behandlingPath/$behandlingId/vilkar/alder")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val alderVilkår = objectMapper.readValue<AlderVilkårDTO>(bodyAsText())
                    alderVilkår.samletUtfall shouldBe SamletUtfallDTO.IKKE_OPPFYLT
                }
            }
        }
    }
}
