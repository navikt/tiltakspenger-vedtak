package no.nav.tiltakspenger.vedtak.routes.rivers

import io.kotest.matchers.shouldBe
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedPersonopplysninger
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SkjermingRoutesTest {
    private companion object {
        const val IDENT = "04927799109"
        const val JOURNALPOSTID = "foobar2"
    }

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val testRapid = TestRapid()
    private val innsendingMediator = InnsendingMediator(
        innsendingRepository = innsendingRepository,
        rapidsConnection = testRapid,
        observatører = listOf(),
    )

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `sjekk at kall til river skjerming route sender ut et behov`() {
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedPersonopplysninger(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    skjermingRoutes(
                        innsendingMediator = innsendingMediator,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$skjermingpath")
                },
            ) {
                setBody(skjermingBody)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("arenatiltak", field(0, "@behov")[0].asText())
        }
    }

    private val skjermingBody = """
        {
            "ident": "$IDENT",
            "journalpostId": "$JOURNALPOSTID",
            "skjerming": {
                "skjermingForPersoner": {
                    "søker": {
                        "ident": "05906398291",
                        "skjerming": false
                    },
                    "barn": []
                },
                "feil": null
            },
            "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()
}
