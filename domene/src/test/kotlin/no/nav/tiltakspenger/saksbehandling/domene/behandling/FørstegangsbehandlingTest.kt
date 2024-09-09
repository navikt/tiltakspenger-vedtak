package no.nav.tiltakspenger.saksbehandling.domene.behandling

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterAvslag
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingUnderBehandlingAvslag
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingUnderBehandlingInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.godkjentAttestering
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler123
import org.junit.jupiter.api.Test

internal class FørstegangsbehandlingTest {
    @Test
    fun `ikke lov å sende en behandling til beslutter uten saksbehandler`() {
        val saksbehandler = ObjectMother.saksbehandler()
        val innvilget =
            behandlingUnderBehandlingInnvilget(saksbehandler = saksbehandler)
                .taSaksbehandlerAvBehandlingen(saksbehandler)

        shouldThrow<IllegalStateException> {
            innvilget.tilBeslutning(saksbehandler123())
        }.message.shouldContain(
            "Behandlingen må være under behandling, det innebærer også at en saksbehandler må ta saken før den kan sendes til beslutter. Behandlingsstatus: KLAR_TIL_BEHANDLING.",
        )

        shouldThrow<IkkeImplementertException> {
            val avslag =
                behandlingUnderBehandlingAvslag(saksbehandler = saksbehandler).taSaksbehandlerAvBehandlingen(
                    saksbehandler,
                )
            avslag.tilBeslutning(saksbehandler123())
        }.message shouldBe "Støtter ikke avslag enda."
    }

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
