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
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.innsending.ports.InnsendingRepository
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedTiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.vedtak.InnsendingMediatorImpl
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
    private val behandlingService =
        mockk<no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService>(relaxed = true)
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
    fun `sjekk at kall til river ytelse route ikke sender ut et behov`() {
        val behandling = Førstegangsbehandling.opprettBehandling(
            sakId = SakId.random(),
            søknad = ObjectMother.nySøknad(),
        )
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedTiltak(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )
        every { behandlingService.hentBehandlingForJournalpostId(any()) } returns behandling
        every { behandlingService.leggTilSaksopplysning(any(), any()) } returns Unit

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    ytelseRoutes(
                        innsendingMediator = innsendingMediator,
                        behandlingService = behandlingService,
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
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("fpytelser", field(0, "@behov")[0].asText())
        }
    }

    @Test
    fun `sjekk at kall til river ytelse route ikke tryner med tom tidligere enn fom`() {
        val behandling = Førstegangsbehandling.opprettBehandling(
            sakId = SakId.random(),
            søknad = ObjectMother.nySøknad(),
        )
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedTiltak(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )
        every { behandlingService.hentBehandlingForJournalpostId(any()) } returns behandling
        every { behandlingService.leggTilSaksopplysning(any(), any()) } returns Unit

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    ytelseRoutes(
                        innsendingMediator = innsendingMediator,
                        behandlingService = behandlingService,
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
                setBody(ytelserMedTomFørFom())
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("fpytelser", field(0, "@behov")[0].asText())
        }
    }

    private val ytelseBody = """
        {
            "respons": {
                "saker": [{
                    "gyldighetsperiodeFom": "2022-01-15T00:00:00",
                    "gyldighetsperiodeTom": "2022-02-28T00:00:00",
                    "kravMottattDato": "2004-10-11",
                    "fagsystemSakId": "42",
                    "status": "AVSLU",
                    "sakType": "INDIV",
                    "vedtak": [{
                        "beslutningsDato": "2004-10-11",
                        "vedtakType": "O",
                        "vedtaksperiodeFom": "2004-09-27",
                        "vedtaksperiodeTom": "2004-10-29",
                        "rettighetType": "BASI",
                        "status": "AVSLU"
                    }],
                    "antallDagerIgjen": null,
                    "antallUkerIgjen": null
                }],
                "feil": null
            },
            "ident": "$IDENT",
            "journalpostId": "$JOURNALPOSTID",
            "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()

    private fun ytelserMedTomFørFom() = """
        {
          "respons": {
            "saker": [
            {
              "gyldighetsperiodeFom": "2022-02-10T00:00:00",
              "gyldighetsperiodeTom": "2022-11-30T00:00:00",
              "kravMottattDato": "2022-09-19",
              "fagsystemSakId": "202264805",
              "status": "INAKT",
              "sakType": "INDIV",
              "vedtak": [
                {
                  "beslutningsDato": "2022-09-22",
                  "vedtakType": "O",
                  "vedtaksperiodeFom": "2022-09-01",
                  "vedtaksperiodeTom": "2022-11-30",
                  "rettighetType": "BASI",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2022-07-06",
                  "vedtakType": "O",
                  "vedtaksperiodeFom": "2022-07-01",
                  "vedtaksperiodeTom": "2022-08-31",
                  "rettighetType": "BASI",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2022-05-19",
                  "vedtakType": "O",
                  "vedtaksperiodeFom": "2022-05-10",
                  "vedtaksperiodeTom": "2022-06-30",
                  "rettighetType": "BASI",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2022-02-10",
                  "vedtakType": "O",
                  "vedtaksperiodeFom": "2022-02-10",
                  "vedtaksperiodeTom": "2022-05-09",
                  "rettighetType": "BASI",
                  "status": "AVSLU"
                }
              ],
              "antallDagerIgjen": null,
              "antallUkerIgjen": null
            },
            {
              "gyldighetsperiodeFom": "2007-11-05T00:00:00",
              "gyldighetsperiodeTom": "2007-11-04T00:00:00",
              "kravMottattDato": "2008-01-14",
              "fagsystemSakId": "20085481",
              "status": "INAKT",
              "sakType": "DAGP",
              "vedtak": [
                {
                  "beslutningsDato": "2009-03-17",
                  "vedtakType": "S",
                  "vedtaksperiodeFom": "2009-03-17",
                  "vedtaksperiodeTom": null,
                  "rettighetType": "DAGO",
                  "status": "IVERK"
                },
                {
                  "beslutningsDato": "2008-03-17",
                  "vedtakType": "N",
                  "vedtaksperiodeFom": "2008-03-17",
                  "vedtaksperiodeTom": null,
                  "rettighetType": "DAGO",
                  "status": "IVERK"
                },
                {
                  "beslutningsDato": "2008-02-27",
                  "vedtakType": "T",
                  "vedtaksperiodeFom": "2007-11-05",
                  "vedtaksperiodeTom": "2007-11-04",
                  "rettighetType": "DAGO",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2008-02-27",
                  "vedtakType": "E",
                  "vedtaksperiodeFom": "2007-11-05",
                  "vedtaksperiodeTom": "2009-03-16",
                  "rettighetType": "DAGO",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2008-01-21",
                  "vedtakType": "F",
                  "vedtaksperiodeFom": "2007-12-05",
                  "vedtaksperiodeTom": "2008-01-29",
                  "rettighetType": "DAGO",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2008-01-21",
                  "vedtakType": "O",
                  "vedtaksperiodeFom": "2007-12-05",
                  "vedtaksperiodeTom": "2007-11-04",
                  "rettighetType": "DAGO",
                  "status": "AVSLU"
                }
              ],
              "antallDagerIgjen": 203,
              "antallUkerIgjen": 40
            },
            {
              "gyldighetsperiodeFom": "2007-02-07T00:00:00",
              "gyldighetsperiodeTom": "2007-03-05T00:00:00",
              "kravMottattDato": "2007-02-09",
              "fagsystemSakId": "200749871",
              "status": "INAKT",
              "sakType": "DAGP",
              "vedtak": [
                {
                  "beslutningsDato": "2007-03-06",
                  "vedtakType": "S",
                  "vedtaksperiodeFom": "2007-03-06",
                  "vedtaksperiodeTom": null,
                  "rettighetType": "DAGO",
                  "status": "IVERK"
                },
                {
                  "beslutningsDato": "2007-02-13",
                  "vedtakType": "O",
                  "vedtaksperiodeFom": "2007-02-07",
                  "vedtaksperiodeTom": "2007-03-05",
                  "rettighetType": "DAGO",
                  "status": "AVSLU"
                }
              ],
              "antallDagerIgjen": 203,
              "antallUkerIgjen": 40
            }
          ],
          "feil": null
          },
          "ident": "$IDENT",
          "journalpostId": "$JOURNALPOSTID",
          "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()
}
