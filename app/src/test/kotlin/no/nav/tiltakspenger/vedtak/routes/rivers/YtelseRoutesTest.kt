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

    @Test
    fun `sjekk at kall til river ytelse route ikke tryner med tom tidligere enn fom`() {
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
                setBody(ytelserMedTomFørFom())
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

    private fun ytelserMedTomFørFom() = """
        {
          "ytelser": [
            {
              "fomGyldighetsperiode": "2022-02-10T00:00:00",
              "tomGyldighetsperiode": "2022-11-30T00:00:00",
              "datoKravMottatt": "2022-09-19",
              "dataKravMottatt": "Individstønad",
              "fagsystemSakId": "202264805",
              "status": "INAKT",
              "ytelsestype": "INDIV",
              "vedtak": [
                {
                  "beslutningsDato": "2022-09-22",
                  "periodetypeForYtelse": "O",
                  "vedtaksperiodeFom": "2022-09-01",
                  "vedtaksperiodeTom": "2022-11-30",
                  "vedtaksType": "BASI",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2022-07-06",
                  "periodetypeForYtelse": "O",
                  "vedtaksperiodeFom": "2022-07-01",
                  "vedtaksperiodeTom": "2022-08-31",
                  "vedtaksType": "BASI",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2022-05-19",
                  "periodetypeForYtelse": "O",
                  "vedtaksperiodeFom": "2022-05-10",
                  "vedtaksperiodeTom": "2022-06-30",
                  "vedtaksType": "BASI",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2022-02-10",
                  "periodetypeForYtelse": "O",
                  "vedtaksperiodeFom": "2022-02-10",
                  "vedtaksperiodeTom": "2022-05-09",
                  "vedtaksType": "BASI",
                  "status": "AVSLU"
                }
              ],
              "antallDagerIgjen": null,
              "antallUkerIgjen": null
            },
            {
              "fomGyldighetsperiode": "2007-11-05T00:00:00",
              "tomGyldighetsperiode": "2007-11-04T00:00:00",
              "datoKravMottatt": "2008-01-14",
              "dataKravMottatt": "Dagpenger",
              "fagsystemSakId": "20085481",
              "status": "INAKT",
              "ytelsestype": "DAGP",
              "vedtak": [
                {
                  "beslutningsDato": "2009-03-17",
                  "periodetypeForYtelse": "S",
                  "vedtaksperiodeFom": "2009-03-17",
                  "vedtaksperiodeTom": null,
                  "vedtaksType": "DAGO",
                  "status": "IVERK"
                },
                {
                  "beslutningsDato": "2008-03-17",
                  "periodetypeForYtelse": "N",
                  "vedtaksperiodeFom": "2008-03-17",
                  "vedtaksperiodeTom": null,
                  "vedtaksType": "DAGO",
                  "status": "IVERK"
                },
                {
                  "beslutningsDato": "2008-02-27",
                  "periodetypeForYtelse": "T",
                  "vedtaksperiodeFom": "2007-11-05",
                  "vedtaksperiodeTom": "2007-11-04",
                  "vedtaksType": "DAGO",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2008-02-27",
                  "periodetypeForYtelse": "E",
                  "vedtaksperiodeFom": "2007-11-05",
                  "vedtaksperiodeTom": "2009-03-16",
                  "vedtaksType": "DAGO",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2008-01-21",
                  "periodetypeForYtelse": "F",
                  "vedtaksperiodeFom": "2007-12-05",
                  "vedtaksperiodeTom": "2008-01-29",
                  "vedtaksType": "DAGO",
                  "status": "AVSLU"
                },
                {
                  "beslutningsDato": "2008-01-21",
                  "periodetypeForYtelse": "O",
                  "vedtaksperiodeFom": "2007-12-05",
                  "vedtaksperiodeTom": "2007-11-04",
                  "vedtaksType": "DAGO",
                  "status": "AVSLU"
                }
              ],
              "antallDagerIgjen": 203,
              "antallUkerIgjen": 40
            },
            {
              "fomGyldighetsperiode": "2007-02-07T00:00:00",
              "tomGyldighetsperiode": "2007-03-05T00:00:00",
              "datoKravMottatt": "2007-02-09",
              "dataKravMottatt": "Dagpenger",
              "fagsystemSakId": "200749871",
              "status": "INAKT",
              "ytelsestype": "DAGP",
              "vedtak": [
                {
                  "beslutningsDato": "2007-03-06",
                  "periodetypeForYtelse": "S",
                  "vedtaksperiodeFom": "2007-03-06",
                  "vedtaksperiodeTom": null,
                  "vedtaksType": "DAGO",
                  "status": "IVERK"
                },
                {
                  "beslutningsDato": "2007-02-13",
                  "periodetypeForYtelse": "O",
                  "vedtaksperiodeFom": "2007-02-07",
                  "vedtaksperiodeTom": "2007-03-05",
                  "vedtaksType": "DAGO",
                  "status": "AVSLU"
                }
              ],
              "antallDagerIgjen": 203,
              "antallUkerIgjen": 40
            }
          ],
          "ident": "$IDENT",
          "journalpostId": "$JOURNALPOSTID",
          "innhentet": "2022-08-22T14:59:46.491437009"
        }
    """.trimIndent()
}
