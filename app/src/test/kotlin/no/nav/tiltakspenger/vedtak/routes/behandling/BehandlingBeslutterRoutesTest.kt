package no.nav.tiltakspenger.vedtak.routes.behandling

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerMedAdmin
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerMedKode6
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.service.personopplysning.PersonopplysningServiceImpl
import no.nav.tiltakspenger.vedtak.service.søker.SøkerServiceImpl
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class BehandlingBeslutterRoutesTest {
    private val innloggetSaksbehandlerProviderMock = mockk<InnloggetSaksbehandlerProvider>()
    private val behandlingService = mockk<BehandlingServiceImpl>()
    private val personopplysningService = mockk<PersonopplysningServiceImpl>()
    private val søkerService = mockk<SøkerServiceImpl>()

    @Test
    fun `sjekk at man ikke kan se behandlinger for en person som er fortrolig uten tilgang`() {
        val person = listOf(personopplysningKjedeligFyr(fortrolig = true))
        val behandlinger = ObjectMother.sakMedOpprettetBehandling(
            personopplysninger = SakPersonopplysninger(person),
        ).behandlinger
        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
        every { behandlingService.hentBehandlingForIdent(any()) } returns behandlinger.filterIsInstance<Førstegangsbehandling>()
        every { personopplysningService.hent(any()) } returns SakPersonopplysninger(person)
        every { søkerService.hentIdent(any(), any()) } returns person.first().ident

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    behandlingBenkRoutes(
                        innloggetSaksbehandlerProviderMock,
                        behandlingService,
                        personopplysningService,
                        søkerService,
                    )
                }
            }
            val respons = defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingerPath/hentForIdent/soker_01HCM4KW1K608E1EQT8JJX11J5")
                },
            ) {
                setBody(identJson)
            }

            respons.status shouldBe HttpStatusCode.OK
            JSONAssert.assertEquals(
                """[]""".trimIndent(),
                respons.bodyAsText(),
                JSONCompareMode.LENIENT,
            )
        }
    }

    @Test
    fun `sjekk at man kan se behandlinger for en person som er fortrolig med tilgang`() {
        val person = listOf(personopplysningKjedeligFyr(strengtFortrolig = true))
        val behandlinger = ObjectMother.sakMedOpprettetBehandling(
            ident = person.first().ident,
            personopplysninger = SakPersonopplysninger(person),
        ).behandlinger
        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandlerMedKode6()
        every { behandlingService.hentBehandlingForIdent(any()) } returns behandlinger.filterIsInstance<Førstegangsbehandling>()
        every { personopplysningService.hent(any()) } returns SakPersonopplysninger(person)
        every { søkerService.hentIdent(any(), any()) } returns person.first().ident

        testApplication {
            application {
                jacksonSerialization()
                routing {
                    behandlingBenkRoutes(
                        innloggetSaksbehandlerProviderMock,
                        behandlingService,
                        personopplysningService,
                        søkerService,
                    )
                }
            }
            val respons = defaultRequest(
                HttpMethod.Get,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingerPath/hentForIdent/soker_01HCM4KW1K608E1EQT8JJX11J5")
                },
            ) {
                setBody(identJson)
            }

            respons.status shouldBe HttpStatusCode.OK
            JSONAssert.assertEquals(
                """
                  [{"id":"${behandlinger.first().id}","ident":"${person.first().ident}","fom":"2023-01-01","tom":"2023-01-31","typeBehandling":"Førstegangsbehandling","status":"Klar til behandling","saksbehandler":null,"beslutter":null}]
                """.trimIndent(),
                respons.bodyAsText(),
                JSONCompareMode.LENIENT,
            )
        }
    }

    @Test
    fun `sjekk at man ikke kan sende inn uten beslutter rolle`() {
        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()

        val behandlingId = BehandlingId.random()
        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    behandlingBeslutterRoutes(
                        innloggetSaksbehandlerProviderMock,
                        behandlingService,
                    )
                }
            }
            shouldThrow<IllegalStateException> {
                defaultRequest(
                    HttpMethod.Post,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$behandlingPath/sendtilbake/$behandlingId")
                    },
                ) {
                    setBody(begrunnelseJson)
                }
            }
        }
    }

    @Test
    fun `sjekk at begrunnelse kan sendes inn`() {
        val begrunnelse = slot<String>()
        val bId = slot<BehandlingId>()
        val saksbehandler = slot<String>()
        val erAdmin = slot<Boolean>()

        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns beslutter()
        every {
            behandlingService.sendTilbakeTilSaksbehandler(
                capture(bId),
                capture(saksbehandler),
                capture(begrunnelse),
                capture(erAdmin),
            )
        } returns Unit

        val behandlingId = BehandlingId.random()
        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    behandlingBeslutterRoutes(
                        innloggetSaksbehandlerProviderMock,
                        behandlingService,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/sendtilbake/$behandlingId")
                },
            ) {
                setBody(begrunnelseJson)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        bId.captured shouldBe behandlingId
        saksbehandler.captured shouldBe "Z12345"
        begrunnelse.captured shouldBe "begrunnelse"
        erAdmin.captured shouldBe false
    }

    @Test
    fun `sjekk at man kan sende tilbake med admin rolle`() {
        val begrunnelse = slot<String>()
        val bId = slot<BehandlingId>()
        val saksbehandler = slot<String>()
        val erAdmin = slot<Boolean>()

        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandlerMedAdmin()
        every {
            behandlingService.sendTilbakeTilSaksbehandler(
                capture(bId),
                capture(saksbehandler),
                capture(begrunnelse),
                capture(erAdmin),
            )
        } returns Unit

        val behandlingId = BehandlingId.random()
        testApplication {
            application {
                // vedtakTestApi()
                jacksonSerialization()
                routing {
                    behandlingBeslutterRoutes(
                        innloggetSaksbehandlerProviderMock,
                        behandlingService,
                    )
                }
            }
            defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$behandlingPath/sendtilbake/$behandlingId")
                },
            ) {
                setBody(begrunnelseJson)
            }
                .apply {
                    status shouldBe HttpStatusCode.OK
                }
        }
        bId.captured shouldBe behandlingId
        saksbehandler.captured shouldBe "Z12345"
        begrunnelse.captured shouldBe "begrunnelse"
        erAdmin.captured shouldBe true
    }

    private val identJson = """
        {
            "ident": "01234567890"
        }
    """.trimIndent()

    private val begrunnelseJson = """
        {
            "begrunnelse": "begrunnelse"
        }
    """.trimIndent()
}
