package no.nav.tiltakspenger.saksbehandling.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerUtenTilgang
import no.nav.tiltakspenger.objectmothers.førstegangsbehandlingTilBeslutter
import no.nav.tiltakspenger.servicemothers.withBehandlingService
import org.junit.jupiter.api.Test

internal class BehandlingServiceTest {
    @Test
    fun `må ha beslutterrolle for å ta behandling som er til beslutning`() {
        with(TestApplicationContext()) {
            val sak = this.førstegangsbehandlingTilBeslutter()
            val behandlingId = sak.førstegangsbehandling.id
            this.withBehandlingService { service ->
                shouldThrow<IllegalStateException> {
                    service.taBehandling(behandlingId, saksbehandlerUtenTilgang())
                }.message shouldBe
                    "Saksbehandler må ha beslutterrolle. Utøvende saksbehandler: Saksbehandler(navIdent='U12345', brukernavn='*****', epost='*****', roller=Roller(value=[]))"
                shouldNotThrow<IllegalStateException> {
                    service.taBehandling(behandlingId, beslutter())
                }
            }
        }
    }

    @Test
    fun `sjekk at man ikke kan sende tilbake uten beslutter rolle`() {
        with(TestApplicationContext()) {
            withBehandlingService { service ->
                val sak = this.førstegangsbehandlingTilBeslutter()
                val behandlingId = sak.førstegangsbehandling.id
                val beslutter = beslutter()
                this.førstegangsbehandlingContext.behandlingService.taBehandling(behandlingId, beslutter)
                shouldThrow<IllegalStateException> {
                    service.sendTilbakeTilSaksbehandler(behandlingId, saksbehandlerUtenTilgang(), "begrunnelse")
                }.message shouldBe "utøvende saksbehandler må være beslutter"

                shouldNotThrow<IllegalStateException> {
                    service.sendTilbakeTilSaksbehandler(behandlingId, beslutter, "begrunnelse")
                }
            }
        }
    }
}
