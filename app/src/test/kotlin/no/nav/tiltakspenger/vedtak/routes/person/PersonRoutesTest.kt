package no.nav.tiltakspenger.vedtak.routes.person

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.util.*
import no.nav.tiltakspenger.vedtak.repository.søker.InMemorySøkerRepository
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.PersonServiceImpl
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

suspend fun ApplicationTestBuilder.defaultRequest(
    method: HttpMethod,
    uri: String,
    setup: HttpRequestBuilder.() -> Unit = {},
): HttpResponse {
    return this.client.request(uri) {
        this.method = method
        this.headers {
            append(HttpHeaders.XCorrelationId, "DEFAULT_CALL_ID")
        }
        setup()
    }
}

class PersonRoutesTest {
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
                    parameters.append("ident", "1234")
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
            "bosted": "NORGE"
          },
          {
            "fornavn": "Emma",
            "etternavn": "Flaks",
            "ident": "987655",
            "bosted": "NORGE"
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
