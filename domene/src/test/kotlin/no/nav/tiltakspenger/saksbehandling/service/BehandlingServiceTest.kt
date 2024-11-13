package no.nav.tiltakspenger.saksbehandling.service

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
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

            this.førstegangsbehandlingContext.behandlingService.taBehandling(
                behandlingId,
                saksbehandlerUtenTilgang(),
                correlationId = CorrelationId.generate(),
            ).shouldBeLeft()

            val sakEksempel2Test = this.sakContext.sakRepo.hentForSakId(sak.id)!!
            println(sakEksempel2Test.førstegangsbehandling)

            this.førstegangsbehandlingContext.behandlingService.taBehandling(
                behandlingId,
                beslutter(),
                correlationId = CorrelationId.generate(),
            ).shouldBeRight()
        }
    }

    @Test
    fun `sjekk at man ikke kan sende tilbake uten beslutter rolle`() = runTest {
        with(TestApplicationContext()) {
            val sak = this.førstegangsbehandlingTilBeslutter()
            val behandlingId = sak.førstegangsbehandling.id
            val beslutter = beslutter()
            this.førstegangsbehandlingContext.behandlingService.taBehandling(behandlingId, beslutter, correlationId = CorrelationId.generate())

            this.førstegangsbehandlingContext.behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandlerUtenTilgang(), "begrunnelse", correlationId = CorrelationId.generate()).shouldBeLeft()

            this.førstegangsbehandlingContext.behandlingService.sendTilbakeTilSaksbehandler(behandlingId, beslutter, "begrunnelse", correlationId = CorrelationId.generate()).shouldBeRight()
        }
    }
}
