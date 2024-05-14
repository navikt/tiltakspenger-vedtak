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
import no.nav.tiltakspenger.innsending.ports.InnsendingRepository
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedForeldrepenger
import no.nav.tiltakspenger.vedtak.InnsendingMediatorImpl
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.helper.InnloggetSystembrukerUtenRollerProvider
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OvergangsstønadRoutesTest {
    private companion object {
        const val IDENT = "04927799109"
        const val JOURNALPOSTID = "foobar2"
    }

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val testRapid = TestRapid()
    private val innsendingMediator = InnsendingMediatorImpl(
        innsendingRepository = innsendingRepository,
        rapidsConnection = testRapid,
        observatører = listOf(),
    )

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `sjekk at kall til river overgangsstønad route sender ut et behov`() {
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedForeldrepenger(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    overgangsstønadRoutes(
                        innsendingMediator = innsendingMediator,
                        InnloggetSystembrukerUtenRollerProvider,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$overgangsstønadPath")
                },
            ) {
                setBody(overgangsstønadBody)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("uføre", field(0, "@behov")[0].asText())
        }
    }

    @Test
    fun `sjekk at kall til river overgangsstønad route med tom liste sender ut et behov`() {
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedForeldrepenger(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    overgangsstønadRoutes(
                        innsendingMediator = innsendingMediator,
                        InnloggetSystembrukerUtenRollerProvider,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$overgangsstønadPath")
                },
            ) {
                setBody(tomBody)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("uføre", field(0, "@behov")[0].asText())
        }
    }

    @Test
    fun `sjekk at kall til river overgangsstønad route med feil ikke sender ut et behov`() {
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedForeldrepenger(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    overgangsstønadRoutes(
                        innsendingMediator = innsendingMediator,
                        InnloggetSystembrukerUtenRollerProvider,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$overgangsstønadPath")
                },
            ) {
                setBody(feilBody)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(0, size)
        }
    }

    private val tomBody = """
        {
            "ident": "$IDENT",
            "journalpostId": "$JOURNALPOSTID",
            "overgangsstønadRespons": {
              "overgangsstønader": [],
              "feil": null
            },
            "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()

    private val feilBody = """
        {
            "ident": "$IDENT",
            "journalpostId": "$JOURNALPOSTID",
            "overgangsstønadRespons": {
              "overgangsstønader": null,
              "feil": "Feilet"
            },
            "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()

    private val overgangsstønadBody = """
        {
            "ident": "$IDENT",
            "journalpostId": "$JOURNALPOSTID",
            "overgangsstønadRespons": {
              "overgangsstønader": [
                {
                  "fom": "2022-01-01",
                  "tom": "2022-01-31",  
                  "datakilde": "EF"
                }
              ],
              "feil": null
            },
            "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()
}
