package no.nav.tiltakspenger.saksbehandling.domene.behandling

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterAvslag
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.godkjentAttestering
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler123
import org.junit.jupiter.api.Test

internal class FørstegangsbehandlingTest {
    @Test
    fun `ikke lov å iverksette en behandling uten beslutter`() {
        val innvilget = behandlingTilBeslutterInnvilget(saksbehandler123())

        shouldThrow<IllegalStateException> {
            innvilget.iverksett(saksbehandler123(), godkjentAttestering())
        }.message shouldBe "Må ha status UNDER_BESLUTNING for å iverksette. Behandlingsstatus: KLAR_TIL_BESLUTNING"

        shouldThrow<IkkeImplementertException> {
            val avslag = behandlingTilBeslutterAvslag()
            avslag.iverksett(saksbehandler123(), godkjentAttestering())
        }.message shouldBe "Støtter ikke avslag enda."
    }
}
