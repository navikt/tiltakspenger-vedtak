package no.nav.tiltakspenger.vedtak.routes.søknad

import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
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
import no.nav.tiltakspenger.vedtak.service.søknad.BehandlingDTO
import no.nav.tiltakspenger.vedtak.service.søknad.KommunaleVilkårsVurderingsKategoriDTO
import no.nav.tiltakspenger.vedtak.service.søknad.PeriodeDTO
import no.nav.tiltakspenger.vedtak.service.søknad.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.service.søknad.SøknadDTO
import no.nav.tiltakspenger.vedtak.service.søknad.SøknadService
import no.nav.tiltakspenger.vedtak.service.søknad.UtfallDTO
import no.nav.tiltakspenger.vedtak.service.søknad.VilkårsVurderingsKategoriDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SøknadRoutesTest {

    private val søknadServiceMock = mockk<SøknadService>()
    private val innloggetSaksbehandlerProviderMock = mockk<InnloggetSaksbehandlerProvider>()

    @Test
    fun `should respond with ok`() {

        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        every { søknadServiceMock.hentBehandlingAvSøknad("1234", any()) } returns BehandlingDTO(
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
        testApplication {
            application {
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        innloggetSaksbehandlerProviderMock, søknadServiceMock
                    )
                }
            }

            defaultRequest(HttpMethod.Get, url {
                protocol = URLProtocol.HTTPS
                path("$søknadPath/1234")
            }).apply {
                status shouldBe HttpStatusCode.OK
            }
        }
    }

    @Test
    fun `at saksbebandler ikke har tilgang burde gi forbidden`() {

        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        every { søknadServiceMock.hentBehandlingAvSøknad("1234", any()) } throws TilgangException("test")

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        innloggetSaksbehandlerProviderMock, søknadServiceMock
                    )
                }
            }

            defaultRequest(HttpMethod.Get, url {
                protocol = URLProtocol.HTTPS
                path("$søknadPath/1234")
            }).apply {
                status shouldBe HttpStatusCode.Forbidden
            }
        }
    }

    @Test
    fun `should respond with not found`() {
        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        testApplication {
            application {
                jacksonSerialization()
                routing {
                    søknadRoutes(
                        innloggetSaksbehandlerProviderMock, søknadServiceMock
                    )
                }
            }

            defaultRequest(HttpMethod.Get, url {
                protocol = URLProtocol.HTTPS
                path("$søknadPath")
            }, setup = {}).apply {
                status shouldBe HttpStatusCode.NotFound
            }
        }
    }
}
