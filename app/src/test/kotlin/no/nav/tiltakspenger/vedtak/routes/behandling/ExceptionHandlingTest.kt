package no.nav.tiltakspenger.vedtak.routes.behandling

import io.kotest.matchers.shouldBe
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.vedtak.exceptions.ManglendeJWTTokenException
import no.nav.tiltakspenger.vedtak.routes.configureExceptions
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class ExceptionHandlingTest {
    private val innloggetSaksbehandlerProviderMock = mockk<InnloggetSaksbehandlerProvider>()
    private val behandlingService = mockk<BehandlingServiceImpl>()
    private val søkerService = mockk<SøkerService>()

    @Test
    fun `Manglende token skal bli til 401`() {
        every { innloggetSaksbehandlerProviderMock.krevInnloggetSaksbehandler(any()) } throws ManglendeJWTTokenException()
        every { behandlingService.hentAlleBehandlinger(any()) } throws IllegalStateException("Wuzza")

        val exceptedStatusCode = HttpStatusCode.Unauthorized
        val expectedBody = """
        {
          "status": 401,
          "title": "ManglendeJWTTokenException",
          "detail": "JWTToken ikke funnet"
        }
        """.trimIndent()

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                configureExceptions()
                routing {
                    behandlingBenkRoutes(
                        innloggetSaksbehandlerProviderMock,
                        behandlingService,
                        søkerService,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingerPath")
                },
            ).apply {
                status shouldBe exceptedStatusCode
                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")
                JSONAssert.assertEquals(
                    expectedBody,
                    bodyAsText(),
                    JSONCompareMode.LENIENT,
                )
            }
        }
    }

    @Test
    fun `IllegalStateException skal bli til 500`() {
        every { innloggetSaksbehandlerProviderMock.krevInnloggetSaksbehandler(any()) } returns ObjectMother.beslutter()
        every { behandlingService.hentAlleBehandlinger(any()) } throws IllegalStateException("Wuzza")

        val exceptedStatusCode = HttpStatusCode.InternalServerError
        val expectedBody = """
        {
          "status": 500,
          "title": "IllegalStateException",
          "detail": "Wuzza"
        }
        """.trimIndent()

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                configureExceptions()
                routing {
                    behandlingBenkRoutes(
                        innloggetSaksbehandlerProviderMock,
                        behandlingService,
                        søkerService,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingerPath")
                },
            ).apply {
                status shouldBe exceptedStatusCode
                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")
                JSONAssert.assertEquals(
                    expectedBody,
                    bodyAsText(),
                    JSONCompareMode.LENIENT,
                )
            }
        }
    }
}
