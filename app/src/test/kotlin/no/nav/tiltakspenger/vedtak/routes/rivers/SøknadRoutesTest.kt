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
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
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
    private val sakRepo = mockk<SakRepo>(relaxed = true)
    private val behandlingRepo = mockk<BehandlingRepo>(relaxed = true)
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

    private val behandlingService = BehandlingServiceImpl(behandlingRepo)
    private val sakService = SakServiceImpl(sakRepo, behandlingRepo, behandlingService)

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

        every { sakRepo.hentForIdentMedPeriode(any(), any()) } returns emptyList()
        every { sakRepo.lagre(any()) } returnsArgument 0

        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    søknadRoutes(
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
                    path("$søknadpath")
                },
            ) {
                setBody(søknadBodyV3)
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

    private val søknadBodyV3 = """
        {
            "versjon": "3",
            "søknadId": "735ac33a-bf3b-43c0-a331-e9ac99bdd6f8",
            "dokInfo": {
              "journalpostId": "$JOURNALPOSTID",
              "dokumentInfoId": "987",
              "filnavn": "tiltakspengersoknad.json"
            },
            "personopplysninger": {
              "ident": "$IDENT",
              "fornavn": "NØDVENDIG",
              "etternavn": "HOFTE"
            },
            "arenaTiltak": {
              "arenaId": "123",
              "arrangoernavn": "Testarrangør",
              "tiltakskode": "Annen utdanning",
              "opprinneligSluttdato": "2025-04-10",
              "opprinneligStartdato": "2025-04-01",
              "sluttdato": "2025-04-10",
              "startdato": "2025-04-01"
            },
            "brukerTiltak": null,
            "barnetilleggPdl": [
              {
                "fødselsdato": "2010-02-13",
                "fornavn": "INKLUDERENDE",
                "mellomnavn": null,
                "etternavn": "DIVA",
                "oppholderSegIEØS": {
                  "svar": "Ja"
                }
              }
            ],
            "barnetilleggManuelle": [],
            "vedlegg": [
              {
                "journalpostId": "123",
                "dokumentInfoId": "456",
                "filnavn": "tiltakspengersoknad.json"
              }
            ],
            "kvp": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "intro": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "institusjon": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "etterlønn": {
              "svar": "Nei"
            },
            "gjenlevendepensjon": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "alderspensjon": {
              "svar": "Nei",
              "fom": null
            },
            "sykepenger": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "supplerendeStønadAlder": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "supplerendeStønadFlyktning": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "jobbsjansen": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "trygdOgPensjon": {
              "svar": "Nei",
              "fom": null,
              "tom": null
            },
            "lønnetArbeid": {
              "svar": "Nei"
            },
            "opprettet": "2023-06-14T21:12:08.447993177"
        }
    """.trimIndent()

//    private val altBody = """
//        {
//          "versjon": "2",
//          "søknadId": "c79a0043-a97c-4f15-bf57-43e5d9c4d0c8",
//          "dokInfo": {
//            "journalpostId": "$JOURNALPOSTID",
//            "dokumentInfoId": "123",
//            "filnavn": "tiltakspengersoknad.json"
//          },
//          "personopplysninger": {
//            "ident": "$IDENT",
//            "fornavn": "RAVGUL",
//            "etternavn": "ENG"
//          },
//          "arenaTiltak": {
//            "arenaId": "123",
//            "arrangoernavn": "Testarrangør",
//            "tiltakskode": "Annen utdanning",
//            "opprinneligSluttdato": null,
//            "opprinneligStartdato": null,
//            "sluttdato": "2023-06-30",
//            "startdato": "2023-06-01"
//          },
//          "brukerTiltak": null,
//          "barnetilleggPdl": [],
//          "barnetilleggManuelle": [
//            {
//              "fødselsdato": "2023-06-01",
//              "fornavn": "Barn",
//              "mellomnavn": null,
//              "etternavn": "Barnesen",
//              "oppholderSegIEØS": {
//                "svar": "Ja"
//              }
//            }
//          ],
//          "vedlegg": [
//            {
//              "journalpostId": "598147706",
//              "dokumentInfoId": "624897237",
//              "filnavn": "tiltakspengersoknad.json"
//            }
//          ],
//          "kvp": {
//            "svar": "Ja",
//            "fom": "2023-06-01",
//            "tom": "2023-06-30"
//          },
//          "intro": {
//            "svar": "Ja",
//            "fom": "2023-06-01",
//            "tom": "2023-06-30"
//          },
//          "institusjon": {
//            "svar": "Ja",
//            "fom": "2023-06-01",
//            "tom": "2023-06-30"
//          },
//          "etterlønn": {
//            "svar": "Ja"
//          },
//          "gjenlevendepensjon": {
//            "svar": "Ja",
//            "fom": "2023-06-01"
//          },
//          "alderspensjon": {
//            "svar": "Ja",
//            "fom": "2023-06-01"
//          },
//          "sykepenger": {
//            "svar": "Ja",
//            "fom": "2023-06-01",
//            "tom": "2023-06-30"
//          },
//          "supplerendeStønadAlder": {
//            "svar": "Ja",
//            "fom": "2023-06-01",
//            "tom": "2023-06-30"
//          },
//          "supplerendeStønadFlyktning": {
//            "svar": "Ja",
//            "fom": "2023-06-01",
//            "tom": "2023-06-30"
//          },
//          "jobbsjansen": {
//            "svar": "Ja",
//            "fom": "2023-06-01",
//            "tom": "2023-06-30"
//          },
//          "trygdOgPensjon": {
//            "svar": "Ja",
//            "fom": "-999999999-01-01"
//          },
//          "opprettet": "2023-06-05T15:13:21.633164273"
//        }
//    """.trimIndent()
//
//    private val søknadBody = """
//        {
//            "versjon": "1",
//            "søknadId": "12304",
//            "dokInfo": {
//              "journalpostId": "$JOURNALPOSTID",
//              "dokumentInfoId": "43",
//              "filnavn": "tiltakspenger.json"
//            },
//            "personopplysninger": {
//              "ident": "$IDENT",
//              "fornavn": "TALENTFULL",
//              "etternavn": "GYNGEHEST"
//            },
//            "arenaTiltak": null,
//            "brukerTiltak": {
//              "tiltakskode": "AMO",
//              "arrangoernavn": "Tiltaksnavn",
//              "beskrivelse": null,
//              "fom": "2022-02-01",
//              "tom": "2022-03-31",
//              "adresse": "Adresse",
//              "postnummer": "0166",
//              "antallDager": 5
//            },
//            "barnetilleggPdl": [],
//            "barnetilleggManuelle": [],
//            "vedlegg": [],
//            "kvp": {
//              "svar": "Nei",
//              "fom": "2022-02-01",
//              "tom": "2022-03-31"
//            },
//            "intro": {
//              "svar": "IkkeBesvart",
//              "fom": null,
//              "tom": null
//            },
//            "institusjon": {
//              "svar": "Nei",
//              "fom": null,
//              "tom": null
//            },
//            "etterlønn": {
//              "svar": "Nei"
//            },
//            "gjenlevendepensjon": {
//              "svar": "IkkeMedISøknaden",
//              "fom": null
//            },
//            "alderspensjon": {
//              "svar": "IkkeMedISøknaden",
//              "fom": null
//            },
//            "sykepenger": {
//              "svar": "IkkeMedISøknaden",
//              "fom": null,
//              "tom": null
//            },
//            "supplerendeStønadAlder": {
//              "svar": "IkkeMedISøknaden",
//              "fom": null,
//              "tom": null
//            },
//            "supplerendeStønadFlyktning": {
//              "svar": "IkkeMedISøknaden",
//              "fom": null,
//              "tom": null
//            },
//            "jobbsjansen": {
//              "svar": "IkkeMedISøknaden",
//              "fom": null,
//              "tom": null
//            },
//            "trygdOgPensjon": {
//              "svar": "IkkeMedISøknaden",
//              "fom": null
//            },
//            "opprettet": "2022-02-08T14:26:42.831"
//          }
//    """.trimIndent()
}
