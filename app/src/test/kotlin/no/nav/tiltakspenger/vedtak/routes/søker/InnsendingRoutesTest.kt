package no.nav.tiltakspenger.vedtak.routes.søker

import io.kotest.matchers.shouldBe
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.util.*
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.domene.november
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.felles.SøkerId
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
                        antallDager = 0,
                        fritekst = null,
                        vedlegg = emptyList(),
                    ),
                    registrerteTiltak = listOf(),
                    vurderingsperiode = PeriodeDTO(fra = 18.november(2022), til = null),
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
