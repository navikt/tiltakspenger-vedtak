package no.nav.tiltakspenger.vedtak.routes.søknad

import io.kotest.matchers.shouldBe
import io.ktor.client.request.setBody
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
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.StorSøknadDTO
import no.nav.tiltakspenger.vedtak.service.SøknadService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class SøknadRoutesTest {

    private val søknadServiceMock = mockk<SøknadService>()

    @Test
    fun `kalle med en ident i body burde svare ok`() {
        val søknadId = SøknadId.random()

        every {
            søknadServiceMock.hentSøknad(
                ident = "1234",
                søknadId = søknadId,
            )
        } returns StorSøknadDTO(
            søknadId = søknadId.toString()
        )

        val e = """
        {
            "søknadId": "$søknadId"
        }
    """.trimIndent()

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        InnloggetBrukerProvider(),
                        søknadServiceMock,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknadPath/$søknadId")
                },
            ) {
                setBody(
                    //language=JSON
                    """
                  {
                    "ident": "1234"
                  }
                """.trimIndent(),
                )
            }.apply {
                status shouldBe HttpStatusCode.OK
                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")
                JSONAssert.assertEquals(
                    e,
                    bodyAsText(),
                    JSONCompareMode.LENIENT
                )
            }.bodyAsText()
        }
    }

    @Test
    fun `kalle med en ident i body som ikke finnes i db burde svare med 404 Not Found`() {

        val søknadId = SøknadId.random()
        every {
            søknadServiceMock.hentSøknad(
                ident = "1234",
                søknadId = søknadId,
            )
        } returns null

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        InnloggetBrukerProvider(),
                        søknadServiceMock,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknadPath/$søknadId")
                },
            ) {
                setBody(
                    //language=JSON
                    """
                  {
                    "ident": "1234"
                  }
                """.trimIndent(),
                )
            }.apply {
                status shouldBe HttpStatusCode.NotFound
                assertEquals("Søknad ikke funnet", bodyAsText())
            }
        }
    }

    @Test
    fun `kalle uten ident i body burde svare med 400 Bad Request`() {
        val søknadId = SøknadId.random()

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        InnloggetBrukerProvider(),
                        søknadServiceMock,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknadPath/$søknadId")
                },
            ) {
                setBody("{}")
            }.apply {
                status shouldBe HttpStatusCode.BadRequest
            }
        }
    }

    private fun expectedSøknad(søknadId: SøknadId) = """
        {
            "søknadId": "$søknadId"
        }
    """.trimIndent()
}
