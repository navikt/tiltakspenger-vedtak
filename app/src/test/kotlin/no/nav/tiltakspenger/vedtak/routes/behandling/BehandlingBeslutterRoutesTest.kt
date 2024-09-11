package no.nav.tiltakspenger.vedtak.routes.behandling

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
import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.førstegangsbehandlingUnderBeslutning
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test

class BehandlingBeslutterRoutesTest {
    private val innloggetSaksbehandlerProviderMock = mockk<InnloggetSaksbehandlerProvider>()

    @Test
    fun `sjekk at begrunnelse kan sendes inn`() {
        with(TestApplicationContext()) {
            val tac = this
            val sak = this.førstegangsbehandlingUnderBeslutning()
            val behandlingId = sak.førstegangsbehandling.id
            every { innloggetSaksbehandlerProviderMock.krevInnloggetSaksbehandler(any()) } returns beslutter()
            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        behandlingBeslutterRoutes(
                            innloggetSaksbehandlerProvider = innloggetSaksbehandlerProviderMock,
                            behandlingService = tac.førstegangsbehandlingContext.behandlingService,
                            auditService = tac.personContext.auditService,
                        )
                    }
                }
                defaultRequest(
                    HttpMethod.Post,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/sendtilbake/$behandlingId")
                    },
                ) {
                    setBody("""{"begrunnelse": "begrunnelse"}""")
                }.apply {
                    status shouldBe HttpStatusCode.OK
                }
            }
            tac.førstegangsbehandlingContext.behandlingRepo.hent(behandlingId).attesteringer.single().let {
                it shouldBe
                    Attestering(
                        // Ignorerer id+tidspunkt
                        id = it.id,
                        tidspunkt = it.tidspunkt,
                        status = no.nav.tiltakspenger.saksbehandling.domene.behandling.Attesteringsstatus.SENDT_TILBAKE,
                        begrunnelse = "begrunnelse",
                        beslutter = beslutter().navIdent,
                    )
            }
        }
    }
}
