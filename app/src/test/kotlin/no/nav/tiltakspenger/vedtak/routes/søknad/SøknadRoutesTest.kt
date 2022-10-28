package no.nav.tiltakspenger.vedtak.routes.søknad

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.util.*
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.søknad.BehandlingDTO
import no.nav.tiltakspenger.vedtak.service.søknad.PeriodeDTO
import no.nav.tiltakspenger.vedtak.service.søknad.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.service.søknad.StorSøknadDTO
import no.nav.tiltakspenger.vedtak.service.søknad.SøknadDTO
import no.nav.tiltakspenger.vedtak.service.søknad.SøknadService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate

class SøknadRoutesTest {

    private val søknadServiceMock = mockk<SøknadService>()

    @Test
    fun `should respond with ok`() {

        every { søknadServiceMock.hentBehandlingAvSøknad("1234") } returns BehandlingDTO(
            personopplysninger = PersonopplysningerDTO(
                fornavn = null,
                etternavn = null,
                ident = "",
                barn = listOf()
            ),
            søknad = SøknadDTO(
                søknadId = "",
                søknadsdato = LocalDate.now(),
                arrangoernavn = null,
                tiltakskode = null,
                startdato = LocalDate.now(),
                sluttdato = null,
                antallDager = 0
            ),
            registrerteTiltak = listOf(),
            vurderingsperiode = PeriodeDTO(fra = LocalDate.now(), til = null),
            vurderinger = listOf()

        )
        testApplication {
            application {
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        InnloggetBrukerProvider(),
                        søknadServiceMock
                    )
                }
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknadPath/1234")
                }
            ).apply {
                status shouldBe HttpStatusCode.OK
            }
        }
    }

    @Test
    fun `should respond with not found`() {
        testApplication {
            application {
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        InnloggetBrukerProvider(),
                        søknadServiceMock
                    )
                }
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknadPath")
                }, setup = {}
            ).apply {
                status shouldBe HttpStatusCode.NotFound
            }
        }
    }

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
                    path("$personsøknadPath/$søknadId")
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
                    path("$personsøknadPath/$søknadId")
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
                    path("$personsøknadPath/$søknadId")
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
