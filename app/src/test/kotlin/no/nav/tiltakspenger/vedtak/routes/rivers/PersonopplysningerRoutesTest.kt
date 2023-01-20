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
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.objectmothers.innsendingMedSøknad
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSystembrukerProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class PersonopplysningerRoutesTest {
    private companion object {
        const val IDENT = "04927799109"
        const val JOURNALPOSTID = "foobar3"
    }

    private val testRapid = TestRapid()

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val innsendingMediator = InnsendingMediator(
        innsendingRepository = innsendingRepository,
        rapidsConnection = testRapid,
        observatører = listOf()
    )

    private val søkerRepository = mockk<SøkerRepository>(relaxed = true)
    private val søkerMediator = SøkerMediator(
        søkerRepository = søkerRepository,
        rapidsConnection = testRapid
    )

    private val innloggetSystembrukerProvider = mockk<InnloggetSystembrukerProvider>()

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `sjekk at kall til river personopplysninger route sender ut behov om skjerming`() {
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedSøknad(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        every { innloggetSystembrukerProvider.hentInnloggetSystembruker(any()) } returns Systembruker(
            brukernavn = "Systembruker",
            roller = listOf(Rolle.LAGE_HENDELSER),
        )

        val personopplysningerMottattHendelse =
            File("src/test/resources/personopplysningerMottattHendelse_ny.json").readText()

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    personopplysningerRoutes(
                        innloggetSystembrukerProvider = innloggetSystembrukerProvider,
                        innsendingMediator = innsendingMediator,
                        søkerMediator = søkerMediator,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$personopplysningerPath")
                },
            ) {
                setBody(personopplysningerMottattHendelse)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("skjerming", field(0, "@behov")[0].asText())
            Assertions.assertEquals("AvventerPersonopplysninger", field(0, "tilstandtype").asText())
            Assertions.assertEquals(IDENT, field(0, "ident").asText())
            Assertions.assertEquals("07085512345", field(0, "barn")[0].asText())
        }
    }
}

//{
//    "@event_name": "behov",
//    "@opprettet": "2023-01-17T12:50:54.875468981",
//    "@id": "f51435b1-c993-4ca8-92ff-f62f3d4f2ebc",
//    "@behovId": "dfe8e0cc-83ab-4182-96f8-6b5a49ce5b8b",
//    "@behov": [
//    "skjerming"
//    ],
//    "journalpostId": "foobar3",
//    "tilstandtype": "AvventerPersonopplysninger",
//    "ident": "04927799109",
//    "barn": [
//    "07085512345"
//    ],
//    "system_read_count": 0,
//    "system_participating_services": [
//    {
//        "id": "f51435b1-c993-4ca8-92ff-f62f3d4f2ebc",
//        "time": "2023-01-17T12:50:54.895176586"
//    }
//    ]
//}
