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
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.november
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.søker.AlderVilkårsvurderingDTO
import no.nav.tiltakspenger.vedtak.service.søker.InstitusjonsoppholdDTO
import no.nav.tiltakspenger.vedtak.service.søker.KlarForBehandlingDTO
import no.nav.tiltakspenger.vedtak.service.søker.KommunaleYtelserDTO
import no.nav.tiltakspenger.vedtak.service.søker.KonklusjonDTO
import no.nav.tiltakspenger.vedtak.service.søker.LønnsinntekterDTO
import no.nav.tiltakspenger.vedtak.service.søker.PensjonsordningerDTO
import no.nav.tiltakspenger.vedtak.service.søker.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.service.søker.StatligeYtelserDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.service.søker.SøknadDTO
import no.nav.tiltakspenger.vedtak.service.søker.TiltakspengerDTO
import no.nav.tiltakspenger.vedtak.service.søker.UtfallDTO
import no.nav.tiltakspenger.vedtak.service.søker.VilkårsvurderingDTO
import no.nav.tiltakspenger.vedtak.service.søker.ÅpenPeriodeDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate

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
            søkerId = "",
            ident = "1234",
            personopplysninger = PersonopplysningerDTO(
                fornavn = "Foo",
                etternavn = "Bar",
                ident = "",
                fødselsdato = LocalDate.now(),
                barn = listOf(),
                fortrolig = false,
                strengtFortrolig = false,
                skjermet = false,
            ),
            behandlinger = listOf(
                KlarForBehandlingDTO(
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
                    vurderingsperiode = ÅpenPeriodeDTO(fra = 18.november(2022), til = null),
                    statligeYtelser = StatligeYtelserDTO(
                        samletUtfall = UtfallDTO.KreverManuellVurdering,
                        aap = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        dagpenger = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        uføre = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        pleiepengerNærstående = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        pleiepengerSyktBarn = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        foreldrepenger = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        svangerskapspenger = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        opplæringspenger = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        omsorgspenger = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        overgangsstønad = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        sykepenger = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        gjenlevendepensjon = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        alderspensjon = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        supplerendeStønad = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        supplerendeStønadFlyktning = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        supplerendeStønadAlder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                    ),
                    kommunaleYtelser = KommunaleYtelserDTO(
                        samletUtfall = UtfallDTO.KreverManuellVurdering,
                        kvp = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                        introProgrammet = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),

                    ),
                    pensjonsordninger = PensjonsordningerDTO(
                        samletUtfall = UtfallDTO.KreverManuellVurdering,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                    ),
                    lønnsinntekt = LønnsinntekterDTO(
                        samletUtfall = UtfallDTO.KreverManuellVurdering,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                    ),
                    institusjonsopphold = InstitusjonsoppholdDTO(
                        samletUtfall = UtfallDTO.KreverManuellVurdering,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                    ),
                    barnetillegg = emptyList(),
                    tiltakspengerYtelser = TiltakspengerDTO(
                        samletUtfall = UtfallDTO.KreverManuellVurdering,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.KreverManuellVurdering,
                            ),
                        ),
                    ),
                    alderVilkårsvurdering = AlderVilkårsvurderingDTO(
                        samletUtfall = UtfallDTO.Oppfylt,
                        perioder = listOf(
                            VilkårsvurderingDTO(
                                kilde = "",
                                detaljer = "",
                                periode = null,
                                kreverManuellVurdering = false,
                                utfall = UtfallDTO.Oppfylt,
                            ),
                        ),
                    ),
                    konklusjon = KonklusjonDTO(), // TODO: Denne mangler innhold
                    hash = "hash",
                ),
            ),
        )

        testApplication {
            application {
                // vedtakTestApi()
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
                    path("$søkerPath/$søkerId")
                },
            ).apply {
                status shouldBe HttpStatusCode.OK
                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")
                println(bodyAsText())
                JSONAssert.assertEquals(
                    expectedSøkerMedSøknader,
                    bodyAsText(),
                    JSONCompareMode.LENIENT,
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
                // vedtakTestApi()
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
                    path("$søkerPath/$søkerId")
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
                // vedtakTestApi()
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
                    path("$søkerPath/$sokerId")
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
                "samletUtfall": "KreverManuellVurdering",
                "aap": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ],
                "dagpenger": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ],
                "uføre": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ],
                "pleiepengerNærstående": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ],
                "pleiepengerSyktBarn": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ],
                "foreldrepenger": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ],
                "svangerskapspenger": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ],
                "omsorgspenger": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ],
                "opplæringspenger": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ]
              },
              "kommunaleYtelser": {
                "samletUtfall": "KreverManuellVurdering",
                "kvp": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ],
                "introProgrammet": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ]
              },
              "pensjonsordninger": {
                "samletUtfall": "KreverManuellVurdering",
                "perioder": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ]
              },
              "lønnsinntekt": {
                "samletUtfall": "KreverManuellVurdering",
                "perioder": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
                  }
                ]
              },
              "institusjonsopphold": {
                "samletUtfall": "KreverManuellVurdering",
                "perioder": [
                  {
                    "kilde": "",
                    "detaljer": "",
                    "periode": null,
                    "kreverManuellVurdering": false,
                    "utfall": "KreverManuellVurdering"
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
