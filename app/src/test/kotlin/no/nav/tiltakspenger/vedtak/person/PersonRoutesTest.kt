package no.nav.tiltakspenger.vedtak.person

import com.papsign.ktor.openapigen.route.apiRouting
import io.kotest.matchers.shouldBe
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import no.nav.tiltakspenger.vedtak.routes.auth
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.routes.openAPI
import no.nav.tiltakspenger.vedtak.routes.person.personPath
import no.nav.tiltakspenger.vedtak.routes.person.personRoutes
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
                openAPI()
                jacksonSerialization()
                apiRouting {
                    auth {
                        personRoutes()
                    }
                }
            }

            defaultRequest(
                HttpMethod.Get,
                "$personPath/test",
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
      "personalia": {
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
