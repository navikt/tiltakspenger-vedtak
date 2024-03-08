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
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedYtelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.saksbehandling.behandling.Søknadsbehandling
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ForeldrepengerVedtakRoutesTest {
    private companion object {
        const val IDENT = "04927799109"
        const val JOURNALPOSTID = "foobar2"
    }

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val testRapid = TestRapid()
    private val behandlingService = mockk<BehandlingService>(relaxed = true)
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
    fun `sjekk at kall til river foreldrepenger route sender ut et behov`() {
        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(
            sakId = SakId.random(),
            søknad = nySøknad(),
        )

        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedYtelse(
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
                    foreldrepengerRoutes(
                        innsendingMediator = innsendingMediator,
                        behandlingService = behandlingService,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path(foreldrepengerpath)
                },
            ) {
                setBody(fpBody)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("overgangsstønad", field(0, "@behov")[0].asText())
        }
    }

    @Test
    fun `sjekk at kall til river foreldrepenger route med tom liste sender ut et behov`() {
        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedYtelse(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    foreldrepengerRoutes(
                        innsendingMediator = innsendingMediator,
                        behandlingService = behandlingService,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path(foreldrepengerpath)
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
            Assertions.assertEquals("overgangsstønad", field(0, "@behov")[0].asText())
        }
    }

    @Test
    fun `sjekk at kall til river foreldrepenger route med null i saksnummer går ok`() {
        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(
            sakId = SakId.random(),
            søknad = nySøknad(),
        )
        every { behandlingService.hentBehandlingForJournalpostId(any()) } returns behandling
        every { behandlingService.leggTilSaksopplysning(any(), any()) } returns Unit

        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsendingMedYtelse(
            ident = IDENT,
            journalpostId = JOURNALPOSTID,
        )

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    foreldrepengerRoutes(
                        innsendingMediator = innsendingMediator,
                        behandlingService = behandlingService,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path(foreldrepengerpath)
                },
            ) {
                setBody(saksnummerNullable)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        with(testRapid.inspektør) {
            Assertions.assertEquals(1, size)
            Assertions.assertEquals("behov", field(0, "@event_name").asText())
            Assertions.assertEquals("overgangsstønad", field(0, "@behov")[0].asText())
        }
    }

    private val tomBody = """
        {
            "ident": "$IDENT",
            "journalpostId": "$JOURNALPOSTID",
            "foreldrepenger": {
              "ytelser": [],
              "feil": null
            },
            "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()

    private val fpBody = """
        {
            "ident": "$IDENT",
            "journalpostId": "$JOURNALPOSTID",
            "foreldrepenger": {
              "ytelser": [
                {
                  "version": "v1",
                  "aktør": "aktørId",
                  "vedtattTidspunkt": "2022-01-01T12:00:00",
                  "ytelse": "PLEIEPENGER_SYKT_BARN",
                  "saksnummer": "sakNr",
                  "vedtakReferanse": "Ref",
                  "ytelseStatus": "LØPENDE",
                  "kildesystem": "FPSAK",
                  "periode": {
                    "fom": "2022-01-01",
                    "tom": "2022-01-15"
                  },
                  "tilleggsopplysninger": "Tillegg",
                  "anvist": [
                      {
                        "periode": {
                          "fom": "2022-01-01",
                          "tom": "2022-01-31"
                        },
                        "beløp": 100.0,
                        "dagsats": 50.0,
                        "utbetalingsgrad": 10.0
                      }
                    ]
                }
              ],
              "feil": null
            },
            "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()

    private val saksnummerNullable = """
        {
          "ident": "$IDENT",
          "journalpostId": "$JOURNALPOSTID",
          "foreldrepenger": {
            "ytelser": [
              {
                "version": "1.0",
                "aktør": "1000002970704",
                "vedtattTidspunkt": "2012-11-13T00:00:00",
                "ytelse": "PLEIEPENGER_SYKT_BARN",
                "saksnummer": null,
                "vedtakReferanse": "",
                "ytelseStatus": "UNDER_BEHANDLING",
                "kildesystem": "K9SAK",
                "periode": {
                  "fom": "2012-11-13",
                  "tom": "9999-12-31"
                },
                "tilleggsopplysninger": null,
                "anvist": []
              }
            ],
            "feil": null
          },
          "innhentet": "2023-02-24T15:15:19.340589406"
        }   
    """.trimIndent()
}
