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
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedSøknad
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepositoryImpl
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
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
        observatører = listOf(),
    )

    private val søkerRepository = mockk<SøkerRepositoryImpl>(relaxed = true)
    private val søkerMediator = SøkerMediator(
        søkerRepository = søkerRepository,
        rapidsConnection = testRapid,
    )

    private val sakService = mockk<SakServiceImpl>(relaxed = true)
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
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    personopplysningerRoutes(
                        innloggetSystembrukerProvider = innloggetSystembrukerProvider,
                        innsendingMediator = innsendingMediator,
                        søkerMediator = søkerMediator,
                        sakService = sakService,
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
