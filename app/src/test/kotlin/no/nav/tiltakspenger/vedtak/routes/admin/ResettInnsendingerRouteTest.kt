package no.nav.tiltakspenger.vedtak.routes.admin

import io.kotest.matchers.shouldBe
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.util.*
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
                //vedtakTestApi()
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
