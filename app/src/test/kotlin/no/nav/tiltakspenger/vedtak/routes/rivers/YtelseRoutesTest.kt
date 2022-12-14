package no.nav.tiltakspenger.vedtak.routes.rivers

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.util.*
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.objectmothers.innsendingMedSkjerming
import no.nav.tiltakspenger.objectmothers.innsendingMedTiltak
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class YtelseRoutesTest {
    private companion object {
        const val IDENT = "04927799109"
        const val JOURNALPOSTID = "foobar2"
    }

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val testRapid = TestRapid()
    private val innsendingMediator = InnsendingMediator(
        innsendingRepository = innsendingRepository,
        rapidsConnection = testRapid,
        observatører = listOf()
    )

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `sjekk at kall til river ytelse route ikke sender ut et behov`() {
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedTiltak(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    ytelseRoutes(
                        innsendingMediator = innsendingMediator
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$ytelsepath")
                },
            ) {
                setBody(ytelseBody)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(0, size)
        }
    }

    private val ytelseBody = """
        {
          "ytelser": [
            {
              "fomGyldighetsperiode": "2022-01-15T00:00:00",
              "tomGyldighetsperiode": "2022-02-28T00:00:00",
              "datoKravMottatt": "2004-10-11",
              "dataKravMottatt": "Individstønad",
              "fagsystemSakId": "42",
              "status": "AVSLU",
              "ytelsestype": "INDIV",
              "vedtak": [
                {
                  "beslutningsDato": "2004-10-11",
                  "periodetypeForYtelse": "O",
                  "vedtaksperiodeFom": "2004-09-27",
                  "vedtaksperiodeTom": "2004-10-29",
                  "vedtaksType": "BASI",
                  "status": "AVSLU"
                }
              ],
              "antallDagerIgjen": null,
              "antallUkerIgjen": null
            }
          ],
          "ident": "$IDENT",
          "journalpostId": "$JOURNALPOSTID",
          "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()
}
