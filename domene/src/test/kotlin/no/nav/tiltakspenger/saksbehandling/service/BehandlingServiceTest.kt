package no.nav.tiltakspenger.saksbehandling.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerUtenTilgang
import no.nav.tiltakspenger.objectmothers.førstegangsbehandlingTilBeslutter

import org.junit.jupiter.api.Test

internal class BehandlingServiceTest {
    @Test
    fun `må ha beslutterrolle for å ta behandling som er til beslutning`() = runTest {
        with(TestApplicationContext()) {
            val sak = this.førstegangsbehandlingTilBeslutter()
            val behandlingId = sak.førstegangsbehandling.id

            shouldThrow<IllegalStateException> {
                this.førstegangsbehandlingContext.behandlingService.taBehandling(
                    behandlingId,
                    saksbehandlerUtenTilgang(),
                    correlationId = CorrelationId.generate()
                )
            }.message shouldBe
                "Saksbehandler må ha beslutterrolle. Utøvende saksbehandler: Saksbehandler(navIdent='U12345', brukernavn='*****', epost='*****', roller=Roller(value=[]))"
            shouldNotThrow<IllegalStateException> {
                this.førstegangsbehandlingContext.behandlingService.taBehandling(
                    behandlingId,
                    beslutter(),
                    correlationId = CorrelationId.generate()
                )
            }

        }
    }

    @Test
    fun `sjekk at man ikke kan sende tilbake uten beslutter rolle`() = runTest {
        with(TestApplicationContext()) {
            val sak = this.førstegangsbehandlingTilBeslutter()
            val behandlingId = sak.førstegangsbehandling.id
            val beslutter = beslutter()
            this.førstegangsbehandlingContext.behandlingService.taBehandling(behandlingId, beslutter, correlationId = CorrelationId.generate())

            shouldThrow<IllegalStateException> {
                this.førstegangsbehandlingContext.behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandlerUtenTilgang(), "begrunnelse", correlationId = CorrelationId.generate())
            }.message shouldBe "utøvende saksbehandler må være beslutter"

            shouldNotThrow<IllegalStateException> {
                this.førstegangsbehandlingContext.behandlingService.sendTilbakeTilSaksbehandler(behandlingId, beslutter, "begrunnelse", correlationId = CorrelationId.generate())
            }
        }
    }
}
