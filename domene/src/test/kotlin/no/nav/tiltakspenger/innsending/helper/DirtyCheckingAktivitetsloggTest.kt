package no.nav.tiltakspenger.innsending.helper

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.innsending.Aktivitetslogg
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean

internal class DirtyCheckingAktivitetsloggTest {

    @Test
    fun `should be dirty`() {
        val isDirty = AtomicBoolean(false)
        val aktivitetslogg = DirtyCheckingAktivitetslogg(no.nav.tiltakspenger.innsending.Aktivitetslogg(), isDirty)
        aktivitetslogg.error("Melding")

        isDirty.get() shouldBe true
    }
}
