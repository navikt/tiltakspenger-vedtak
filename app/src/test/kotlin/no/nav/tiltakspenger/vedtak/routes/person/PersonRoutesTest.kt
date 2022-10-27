package no.nav.tiltakspenger.vedtak.routes.person

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.util.*
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.vedtak.repository.søker.InMemorySøkerRepository
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.PersonService
import no.nav.tiltakspenger.vedtak.service.PersonServiceImpl
import no.nav.tiltakspenger.vedtak.service.SøkerDTO
import no.nav.tiltakspenger.vedtak.service.SøknadDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate
import java.time.Month

suspend fun ApplicationTestBuilder.defaultRequest(
    method: HttpMethod,
    uri: String,
    setup: HttpRequestBuilder.() -> Unit = {},
): HttpResponse {
    return this.client.request(uri) {
        this.method = method
        this.headers {
            append(HttpHeaders.XCorrelationId, "DEFAULT_CALL_ID")
            append(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        setup()
    }
}

class PersonRoutesTest {

    private val personServiceMock = mockk<PersonService>()

    @Test
    fun `should answer 10-4`() {
        testApplication {
            application {
                jacksonSerialization()
                routing {
                    personRoutes(
                        InnloggetBrukerProvider(),
                        PersonServiceImpl(
                            søkerRepository = InMemorySøkerRepository()
                        ),
                    )
                }
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$personPath")
                }, setup = {}
            ).apply {

                status shouldBe HttpStatusCode.OK
                JSONAssert.assertEquals(
                    expected,
                    bodyAsText(),
                    JSONCompareMode.LENIENT
                )
            }
        }
    }

    @Test
    fun `kalle med en ident i body burde svare ok`() {

        every { personServiceMock.hentSøkerOgSøknader("1234") } returns SøkerDTO(
            ident = "1234",
            søknader = listOf(
                SøknadDTO(
                    søknadId = "1234",
                    arrangoernavn = "Ukjent",
                    tiltakskode = "tiltak",
                    startdato = LocalDate.of(2022, Month.DECEMBER, 1),
                    sluttdato = null,
                )
            )
        )

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    personRoutes(
                        InnloggetBrukerProvider(),
                        personServiceMock,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath")
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
                    expectedSøkerMedSøknader,
                    bodyAsText(),
                    JSONCompareMode.LENIENT
                )
            }.bodyAsText()
        }
    }

    @Test
    fun `kalle med en ident i body som ikke finnes i db burde svare med 404 Not Found`() {

        every { personServiceMock.hentSøkerOgSøknader("1234") } returns null

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    personRoutes(
                        InnloggetBrukerProvider(),
                        personServiceMock,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath")
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
                assertEquals("Søker ikke funnet", bodyAsText())
            }
        }
    }

    @Test
    fun `kalle uten ident i body burde svare med 400 Bad Request`() {

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    personRoutes(
                        InnloggetBrukerProvider(),
                        personServiceMock,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath")
                },
            ) {
                setBody("{}")
            }.apply {
                status shouldBe HttpStatusCode.BadRequest
            }
        }
    }

    private val expectedSøkerMedSøknader = """
        {
            "ident": "1234",
            søknader: [
                {
                    "søknadId": "1234",
                    "arrangoernavn": "Ukjent",
                    "tiltakskode": "tiltak",
                    "startdato": "2022-12-01",
                    "sluttdato": null
                }
            ]
        }
    """.trimIndent()

    private val expected = """
     {
      "personopplysninger": {
        "fornavn": "Fornavn",
        "etternavn": "Etternavn",
        "ident": "123454",
        "barn": [
          {
            "fornavn": "Emil",
            "etternavn": "Flaks",
            "ident": "987654",
            "bosted": ""
          },
          {
            "fornavn": "Emma",
            "etternavn": "Flaks",
            "ident": "987655",
            "bosted": ""
          }
        ]
      },
      "behandlinger": [
        {
          "id": "behandlingId",
          "søknad": {},
          "tiltak": {
            "arrangør": "Joblearn",
            "navn": "Gruppe AMO",
            "periode": {
              "fra": "2022-04-01",
              "til": "2022-04-20"
            },
            "prosent": 80,
            "dagerIUken": 4,
            "status": "Godkjent"
          },
          "periode": {
            "fra": "2022-04-01",
            "til": "2022-04-20"
          },
          "vurderinger": [
            {
              "tittel": "Statlige ytelser",
              "utfall": "Uavklart",
              "vilkårsvurderinger": [
                {
                  "utfall": "Oppfylt",
                  "periode": {
                    "fra": "2022-04-01",
                    "til": "2022-04-20"
                  },
                  "vilkår": "Dagpenger",
                  "kilde": "Arena"
                },
                {
                  "utfall": "Oppfylt",
                  "periode": {
                    "fra": "2022-04-01",
                    "til": "2022-04-20"
                  },
                  "vilkår": "AAP",
                  "kilde": "Arena"
                },
                {
                  "utfall": "Uavklart",
                  "periode": {
                    "fra": "2022-04-01",
                    "til": "2022-04-20"
                  },
                  "vilkår": "Tiltakspenger",
                  "kilde": "Arena"
                }
              ]
            }
          ]
        }
      ]
    }
    """.trimMargin()
}
