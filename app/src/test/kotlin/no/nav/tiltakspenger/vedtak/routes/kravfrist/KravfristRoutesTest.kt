package no.nav.tiltakspenger.vedtak.routes.kravfrist

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.felles.exceptions.StøtterIkkeUtfallException
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.førstegangsbehandlingUavklart
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.KravfristVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.kravfristRoutes
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import org.junit.jupiter.api.Test

internal class KravfristRoutesTest {

    private val objectMapper: ObjectMapper = defaultObjectMapper()

    @Test
    fun `test at endepunkt for henting av kravfrist fungerer og blir OPPFYLT`() = runTest {
        with(TestApplicationContext()) {
            val tac = this
            val sak = this.førstegangsbehandlingUavklart()
            val behandlingId = sak.førstegangsbehandling.id

            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        kravfristRoutes(
                            tokenService = tac.tokenService,
                            behandlingService = tac.førstegangsbehandlingContext.behandlingService,
                            auditService = tac.personContext.auditService,
                        )
                    }
                }

                // Sjekk at man kan kjøre Get
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/kravfrist")
                    },
                    jwt = tac.jwtGenerator.createJwtForSaksbehandler(),
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val kravfristVilkår = objectMapper.readValue<KravfristVilkårDTO>(bodyAsText())
                    kravfristVilkår.samletUtfall shouldBe SamletUtfallDTO.OPPFYLT
                }
            }
        }
    }

    @Test
    fun `test at behandlingen ikke kan opprettes om om det er søkt for lenge etter fristen`() = runTest {
        with(TestApplicationContext()) {
            shouldThrow<StøtterIkkeUtfallException> {
                this.førstegangsbehandlingUavklart(Periode(1.januar(2021), 31.januar(2021)))
            }
        }
    }
}
