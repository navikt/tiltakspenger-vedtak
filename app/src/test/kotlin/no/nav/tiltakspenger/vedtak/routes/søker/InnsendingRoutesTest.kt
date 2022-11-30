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
                fortrolig = false,
                strengtFortrolig = false,
                skjermet = false,
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
                        antallDager = 0,
                        fritekst = null,
                        vedlegg = emptyList(),
                    ),
                    registrerteTiltak = listOf(),
                    vurderingsperiode = PeriodeDTO(fra = 18.november(2022), til = null),
                    statligeYtelser = StatligeYtelserDTO(
                        samletUtfall = UtfallDTO.Uavklart,
                        aap = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.Uavklart
                            )
                        ),
                        dagpenger = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.Uavklart
                            )
                        )
                    ),
                    kommunaleYtelser = KommunaleYtelserDTO(
                        samletUtfall = UtfallDTO.Uavklart, kvp = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.Uavklart
                            )
                        ), introProgrammet = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.Uavklart
                            )
                        )

                    ),
                    pensjonsordninger = PensjonsordningerDTO(
                        samletUtfall = UtfallDTO.Uavklart,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.Uavklart
                            )
                        )
                    ),
                    lønnsinntekt = LønnsinntekterDTO(
                        samletUtfall = UtfallDTO.Uavklart,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.Uavklart
                            )
                        )
                    ),
                    institusjonsopphold = InstitusjonsoppholdDTO(
                        samletUtfall = UtfallDTO.Uavklart,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.Uavklart
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
                "antallDager": 0,
                "vedlegg": []
              },
              "registrerteTiltak": [],
              "vurderingsperiode": {
                "fra": "2022-11-18",
                "til": null
              },
              "statligeYtelser": {
                "samletUtfall": "Uavklart",
                "aap": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "Uavklart"
                  }
                ],
                "dagpenger": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "Uavklart"
                  }
                ]
              },
              "kommunaleYtelser": {
                "samletUtfall": "Uavklart",
                "kvp": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "Uavklart"
                  }
                ],
                "introProgrammet": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "Uavklart"
                  }
                ]
              },
              "pensjonsordninger": {
                "samletUtfall": "Uavklart",
                "perioder": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "Uavklart"
                  }
                ]
              },
              "lønnsinntekt": {
                "samletUtfall": "Uavklart",
                "perioder": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "Uavklart"
                  }
                ]
              },
              "institusjonsopphold": {
                "samletUtfall": "Uavklart",
                "perioder": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "Uavklart"
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
