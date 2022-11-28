package no.nav.tiltakspenger.vedtak.routes.søker

import io.kotest.matchers.shouldBe
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.domene.november
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.objectmothers.saksbehandler
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.søker.BehandlingDTO
import no.nav.tiltakspenger.vedtak.service.søker.InstitusjonsoppholdDTO
import no.nav.tiltakspenger.vedtak.service.søker.KommunaleYtelserDTO
import no.nav.tiltakspenger.vedtak.service.søker.LønnsinntekterDTO
import no.nav.tiltakspenger.vedtak.service.søker.PensjonsordningerDTO
import no.nav.tiltakspenger.vedtak.service.søker.PeriodeDTO
import no.nav.tiltakspenger.vedtak.service.søker.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.service.søker.StatligeYtelserDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.service.søker.SøknadDTO
import no.nav.tiltakspenger.vedtak.service.søker.UtfallDTO
import no.nav.tiltakspenger.vedtak.service.søker.VilkårsvurderingDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class InnsendingRoutesTest {

    private val søkerServiceMock = mockk<SøkerService>()
    private val innloggetSaksbehandlerProviderMock = mockk<InnloggetSaksbehandlerProvider>()

    @Test
    fun `kalle med en ident i body burde svare ok`() {
        val søkerId = SøkerId.random()
        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        every {
            søkerServiceMock.hentSøkerOgSøknader(søkerId, saksbehandler())
        } returns SøkerDTO(
            ident = "1234",
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Foo",
                etternavn = "Bar",
                ident = "",
                barn = listOf(),
            ),
            behandlinger = listOf(
                BehandlingDTO(
                    søknad = SøknadDTO(
                        id = "",
                        søknadId = "",
                        søknadsdato = 18.november(2022),
                        arrangoernavn = null,
                        tiltakskode = null,
                        beskrivelse = null,
                        startdato = 18.november(2022),
                        sluttdato = null,
                        antallDager = 0
                    ),
                    registrerteTiltak = listOf(),
                    vurderingsperiode = PeriodeDTO(fra = 18.november(2022), til = null),
                    statligeYtelser = StatligeYtelserDTO(
                        utfall = UtfallDTO.Uavklart,
                        aap = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false
                            )
                        ),
                        dagpenger = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false
                            )
                        )
                    ),
                    kommunaleYtelser = KommunaleYtelserDTO(
                        utfall = UtfallDTO.Uavklart, kvp = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false
                            )
                        ), introProgrammet = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false
                            )
                        )

                    ),
                    pensjonsordninger = PensjonsordningerDTO(
                        utfall = UtfallDTO.Uavklart,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false
                            )
                        )
                    ),
                    lønnsinntekt = LønnsinntekterDTO(
                        utfall = UtfallDTO.Uavklart,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false
                            )
                        )
                    ),
                    institusjonsopphold = InstitusjonsoppholdDTO(
                        utfall = UtfallDTO.Uavklart,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false
                            )
                        )
                    ),
                    barnetillegg = emptyList()
                )
            )
        )

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    søkerRoutes(
                        innloggetSaksbehandlerProviderMock,
                        søkerServiceMock,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath/$søkerId")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")
                println(bodyAsText())
                JSONAssert.assertEquals(
                    expectedSøkerMedSøknader,
                    bodyAsText(),
                    JSONCompareMode.LENIENT
                )
            }
        }
    }

    @Test
    fun `at saksbehandler ikke har tilgang burde svare forbidden`() {
        val søkerId = SøkerId.random()
        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        every {
            søkerServiceMock.hentSøkerOgSøknader(søkerId, saksbehandler())
        } throws TilgangException("Test")

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    søkerRoutes(
                        innloggetSaksbehandlerProviderMock,
                        søkerServiceMock,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath/$søkerId")
                },
            ).apply {
                status shouldBe HttpStatusCode.Forbidden
            }
        }
    }

    @Test
    fun `kalle med en ident i body som ikke finnes i db burde svare med 404 Not Found`() {
        val sokerId = SøkerId.random()
        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        every {
            søkerServiceMock.hentSøkerOgSøknader(sokerId, saksbehandler())
        } returns null

        testApplication {
            application {
                //vedtakTestApi()
                jacksonSerialization()
                routing {
                    søkerRoutes(
                        innloggetSaksbehandlerProviderMock,
                        søkerServiceMock,
                    )
                }
            }

            defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath/$sokerId")
                },
            ).apply {
                status shouldBe HttpStatusCode.NotFound
                assertEquals("Søker ikke funnet", bodyAsText())
            }
        }
    }

    private val expectedSøkerMedSøknader = """
        {
          "ident": "1234",
          "behandlinger": [
            {
              "søknad": {
                "id": "",
                "søknadId": "",
                "søknadsdato": "2022-11-18",
                "arrangoernavn": null,
                "tiltakskode": null,
                "beskrivelse": null,
                "startdato": "2022-11-18",
                "sluttdato": null,
                "antallDager": 0
              },
              "registrerteTiltak": [],
              "vurderingsperiode": {
                "fra": "2022-11-18",
                "til": null
              },
              "statligeYtelser": {
                "utfall": "Uavklart",
                "aap": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false
                  }
                ],
                "dagpenger": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false
                  }
                ]
              },
              "kommunaleYtelser": {
                "utfall": "Uavklart",
                "kvp": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false
                  }
                ],
                "introProgrammet": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false
                  }
                ]
              },
              "pensjonsordninger": {
                "utfall": "Uavklart",
                "perioder": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false
                  }
                ]
              },
              "lønnsinntekt": {
                "utfall": "Uavklart",
                "perioder": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false
                  }
                ]
              },
              "institusjonsopphold": {
                "utfall": "Uavklart",
                "perioder": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false
                  }
                ]
              },
              "barnetillegg": []
            }
          ],
          "personopplysninger": {
            "fornavn": "Foo",
            "etternavn": "Bar",
            "ident": "",
            "barn": []
          }
        }
    """.trimIndent()
}
