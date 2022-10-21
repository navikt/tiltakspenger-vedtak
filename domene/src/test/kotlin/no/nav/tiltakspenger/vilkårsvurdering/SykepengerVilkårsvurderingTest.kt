package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SykepengerVilkårsvurderingTest {

    @Test
    fun `En vilkårsvurdering uten manuell vurdering skal ha utfall KREVER_MANUELL_VURDERING`() {
        val vv = SykepengerVilkårsvurdering()
        val utfall = vv.samletUtfall()
        utfall shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `En vilkårsvurdering med manuell vurdering skal ha utfall gitt av den manuelle vurderingen`() {
        val vv = SykepengerVilkårsvurdering()
        vv.settManuellVurdering(LocalDate.MIN, LocalDate.MAX, Utfall.IKKE_OPPFYLT, "")
        val utfall = vv.samletUtfall()
        utfall shouldBe Utfall.IKKE_OPPFYLT
    }
}
