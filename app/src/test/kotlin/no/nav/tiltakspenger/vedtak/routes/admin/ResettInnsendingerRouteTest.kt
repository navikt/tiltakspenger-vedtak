package no.nav.tiltakspenger.vedtak.routes.admin

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
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.innsending.InnsendingAdminService
import org.junit.jupiter.api.Test

class ResettInnsendingerRouteTest {
    private val innsendingAdminService = mockk<InnsendingAdminService>()

    @Test
    fun `test post mot resettFeiletOgStoppetInnsendinger`() {
        every { innsendingAdminService.resettInnsendingerSomHarFeiletEllerStoppetOpp() } returns Unit

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    resettInnsendingerRoute(innsendingAdminService)
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$resettFeiletOgStoppetInnsendinger")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                bodyAsText() shouldBe "Ok"
            }
        }
    }
}
