package no.nav.tiltakspenger.vedtak.routes.saksbehandler

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
import no.nav.tiltakspenger.objectmothers.saksbehandler
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class SaksbehandlerRoutesTest {
    private val innloggetSaksbehandlerProviderMock = mockk<InnloggetSaksbehandlerProvider>()

    // language = JSON
    private val saksbehandlerMock = """
        {
          "navIdent":"Z12345",
          "brukernavn":"Sak Behandler",
          "epost":"Sak.Behandler@nav.no",
          "roller":["SAKSBEHANDLER"]
        }
    """.trimIndent()

    @Test
    fun `test`() {
        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    saksbehandlerRoutes(
                        innloggetSaksbehandlerProviderMock
                    )
                }
            }
            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$saksbehandlerPath")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")
                JSONAssert.assertEquals(
                    saksbehandlerMock,
                    bodyAsText(),
                    JSONCompareMode.LENIENT
                )
            }
        }
    }
}
