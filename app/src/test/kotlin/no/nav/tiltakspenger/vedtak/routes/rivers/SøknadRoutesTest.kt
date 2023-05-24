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
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingRegistrert
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøker
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SøknadRoutesTest {
    private companion object {
        const val IDENT = "04927799109"
        const val JOURNALPOSTID = "foobar2"
    }

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val søkerRepository = mockk<SøkerRepository>(relaxed = true)
    private val testRapid = TestRapid()
    private val innsendingMediator = InnsendingMediator(
        innsendingRepository = innsendingRepository,
        rapidsConnection = testRapid,
        observatører = listOf(),
    )
    private val søkerMediator = SøkerMediator(
        søkerRepository = søkerRepository,
        rapidsConnection = testRapid,
    )

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `sjekk at kall til river søknad route sender ut et behov`() {
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingRegistrert(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        every { søkerRepository.findByIdent(IDENT) } returns nySøker(
            ident = IDENT,
        )

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        innsendingMediator = innsendingMediator,
                        søkerMediator = søkerMediator,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknadpath")
                },
            ) {
                setBody(søknadBody)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("personopplysninger", field(0, "@behov")[0].asText())
            Assertions.assertEquals(IDENT, field(0, "ident").asText())
        }
    }

    private val søknadBody =
        """
        {
            "versjon": "1",
            "søknadId": "whatever",
            "journalpostId": "$JOURNALPOSTID",
            "dokumentInfoId": "whatever3",
            "filnavn": "filnavn",
            "fornavn": "LEVENDE",
            "etternavn": "POTET",
            "ident": "$IDENT",
            "deltarKvp": false,
            "deltarIntroduksjonsprogrammet": false,
            "introduksjonsprogrammetDetaljer": null,
            "oppholdInstitusjon": false,
            "typeInstitusjon": null,
            "opprettet": "2022-06-29T16:24:02.608",
            "barnetillegg": [],
            "arenaTiltak" : {
                 "arenaId" : "id",
                 "arrangoer" : "navn",
                 "harSluttdatoFraArena" : false,
                 "tiltakskode" : "MENTOR",
                 "erIEndreStatus" : false,
                 "opprinneligSluttdato": null,
                 "opprinneligStartdato" : "2022-06-21",
                 "sluttdato" : "2022-06-29",
                 "startdato" : "2022-06-21"
            },
            "brukerregistrertTiltak": null,
            "trygdOgPensjon" : null,
            "fritekst" : null,
            "vedlegg": []
          }
        """.trimIndent()
}
