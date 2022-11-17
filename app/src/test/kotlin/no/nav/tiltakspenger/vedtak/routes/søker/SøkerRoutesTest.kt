package no.nav.tiltakspenger.vedtak.routes.søker

import io.kotest.matchers.shouldBe
import io.ktor.client.request.setBody
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
import no.nav.tiltakspenger.objectmothers.saksbehandler
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.søker.BehandlingDTO
import no.nav.tiltakspenger.vedtak.service.søker.KommunaleVilkårsVurderingsKategoriDTO
import no.nav.tiltakspenger.vedtak.service.søker.PeriodeDTO
import no.nav.tiltakspenger.vedtak.service.søker.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.service.søker.SøknadDTO
import no.nav.tiltakspenger.vedtak.service.søker.UtfallDTO
import no.nav.tiltakspenger.vedtak.service.søker.VilkårsVurderingsKategoriDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate

class SøkerRoutesTest {

    private val søkerServiceMock = mockk<SøkerService>()
    private val innloggetSaksbehandlerProviderMock = mockk<InnloggetSaksbehandlerProvider>()

    @Test
    fun `kalle med en ident i body burde svare ok`() {

        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        every {
            søkerServiceMock.hentSøkerOgSøknader("1234", saksbehandler())
        } returns SøkerDTO(
            ident = "1234",
            behandlinger = listOf(
                BehandlingDTO(
                    personopplysninger = PersonopplysningerDTO(
                        fornavn = null, etternavn = null, ident = "", barn = listOf()
                    ),
                    søknad = SøknadDTO(
                        søknadId = "",
                        søknadsdato = LocalDate.now(),
                        arrangoernavn = null,
                        tiltakskode = null,
                        beskrivelse = null,
                        startdato = LocalDate.now(),
                        sluttdato = null,
                        antallDager = 0
                    ),
                    registrerteTiltak = listOf(),
                    vurderingsperiode = PeriodeDTO(fra = LocalDate.now(), til = null),
                    statligeYtelser = VilkårsVurderingsKategoriDTO(
                        ytelse = "",
                        lovreferanse = "",
                        utfall = UtfallDTO.Uavklart,
                        detaljer = "",
                        vilkårsvurderinger = listOf()
                    ),
                    kommunaleYtelser = KommunaleVilkårsVurderingsKategoriDTO(
                        ytelse = "",
                        lovreferanse = "",
                        utfall = UtfallDTO.Uavklart,
                        detaljer = "",
                        introProgrammet = emptyList(),
                        kvp = emptyList(),
                    ),
                    pensjonsordninger = VilkårsVurderingsKategoriDTO(
                        ytelse = "",
                        lovreferanse = "",
                        utfall = UtfallDTO.Uavklart,
                        detaljer = "",
                        vilkårsvurderinger = listOf()
                    ),
                    lønnsinntekt = VilkårsVurderingsKategoriDTO(
                        ytelse = "",
                        lovreferanse = "",
                        utfall = UtfallDTO.Uavklart,
                        detaljer = "",
                        vilkårsvurderinger = listOf()
                    ),
                    institusjonsopphold = VilkårsVurderingsKategoriDTO(
                        ytelse = "",
                        lovreferanse = "",
                        utfall = UtfallDTO.Uavklart,
                        detaljer = "",
                        vilkårsvurderinger = listOf()
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
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath")
                },
            ) {
                setBody(
                    //language=JSON
                    """
                  {
                    "ident": "1234"
                  }
                """.trimIndent(),
                )
            }.apply {
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

        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        every {
            søkerServiceMock.hentSøkerOgSøknader("1234", saksbehandler())
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
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath")
                },
            ) {
                setBody(
                    //language=JSON
                    """
                  {
                    "ident": "1234"
                  }
                """.trimIndent(),
                )
            }.apply {
                status shouldBe HttpStatusCode.Forbidden
            }
        }
    }

    @Test
    fun `kalle med en ident i body som ikke finnes i db burde svare med 404 Not Found`() {

        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        every {
            søkerServiceMock.hentSøkerOgSøknader("1234", saksbehandler())
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
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath")
                },
            ) {
                setBody(
                    //language=JSON
                    """
                  {
                    "ident": "1234"
                  }
                """.trimIndent(),
                )
            }.apply {
                status shouldBe HttpStatusCode.NotFound
                assertEquals("Søker ikke funnet", bodyAsText())
            }
        }
    }

    @Test
    fun `kalle uten ident i body burde svare med 400 Bad Request`() {

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
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$søknaderPath")
                },
            ) {
                setBody("{}")
            }.apply {
                status shouldBe HttpStatusCode.BadRequest
            }
        }
    }

    private val expectedSøkerMedSøknader = """
        {
          "ident": "1234",
          "behandlinger": [
            {
              "personopplysninger": {
                "fornavn": null,
                "etternavn": null,
                "ident": "",
                "barn": []
              },
              "søknad": {
                "søknadId": "",
                "søknadsdato": "2022-11-17",
                "arrangoernavn": null,
                "tiltakskode": null,
                "beskrivelse": null,
                "startdato": "2022-11-17",
                "sluttdato": null,
                "antallDager": 0
              },
              "registrerteTiltak": [],
              "vurderingsperiode": {
                "fra": "2022-11-17",
                "til": null
              },
              "statligeYtelser": {
                "ytelse": "",
                "lovreferanse": "",
                "utfall": "Uavklart",
                "detaljer": "",
                "vilkårsvurderinger": []
              },
              "kommunaleYtelser": {
                "ytelse": "",
                "lovreferanse": "",
                "utfall": "Uavklart",
                "detaljer": "",
                "introProgrammet": [],
                "kvp": []
              },
              "pensjonsordninger": {
                "ytelse": "",
                "lovreferanse": "",
                "utfall": "Uavklart",
                "detaljer": "",
                "vilkårsvurderinger": []
              },
              "lønnsinntekt": {
                "ytelse": "",
                "lovreferanse": "",
                "utfall": "Uavklart",
                "detaljer": "",
                "vilkårsvurderinger": []
              },
              "institusjonsopphold": {
                "ytelse": "",
                "lovreferanse": "",
                "utfall": "Uavklart",
                "detaljer": "",
                "vilkårsvurderinger": []
              },
              "barnetillegg": []
            }
          ]
        }
    """.trimIndent()
}
