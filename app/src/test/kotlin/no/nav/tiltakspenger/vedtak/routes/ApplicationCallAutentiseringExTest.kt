package no.nav.tiltakspenger.vedtak.routes

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.vedtak.routes.saksbehandler.SAKSBEHANDLER_PATH
import no.nav.tiltakspenger.vedtak.routes.saksbehandler.saksbehandlerRoutes
import org.junit.jupiter.api.Test

internal class ApplicationCallAutentiseringExTest {

    @Test
    fun cake() {
        runTest {
            testApplication {
                application {
                    routing {
                        saksbehandlerRoutes(
                            // Vi skal ikke komme så langt
                            mockk(),
                        )
                    }
                }
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path(SAKSBEHANDLER_PATH)
                    },
                    jwt = null,
                ).apply {
                    withClue(
                        "Response details:\n" +
                            "Status: ${this.status}\n" +
                            "Content-Type: ${this.contentType()}\n" +
                            "Body: ${this.bodyAsText()}\n",
                    ) {
                        status shouldBe HttpStatusCode.Unauthorized
                        headers["WWW-Authenticate"] shouldBe """Bearer realm="tiltakspenger-vedtak""""
                    }
                }
            }
        }
    }
}
