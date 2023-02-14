package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SykepengerVilkårsvurdering
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class IkkeImplementerteStatligeYtelserVilkårsvurderingTest {

    @Test
    fun `En vilkårsvurdering uten manuell vurdering skal ha utfall IKKE_IMPLEMENTERT`() {
        val vv = SykepengerVilkårsvurdering()
        val utfall = vv.samletUtfall()
        utfall shouldBe Utfall.IKKE_IMPLEMENTERT
    }

    @Test
    fun `En vilkårsvurdering med manuell vurdering skal ha utfall gitt av den manuelle vurderingen`() {
        val vv = SykepengerVilkårsvurdering()
        vv.settManuellVurdering(LocalDate.MIN, LocalDate.MAX, Utfall.IKKE_OPPFYLT, "")
        val utfall = vv.samletUtfall()
        utfall shouldBe Utfall.IKKE_OPPFYLT
    }

    @Test
    fun `En vilkårsvurdering uten manuell vurdering skal ha to vurderinger med kilder`() {
        val vv = SykepengerVilkårsvurdering()
        val vurderinger = vv.vurderinger()
        vurderinger.size shouldBe 1
        vurderinger.first() shouldBe Vurdering(
            vilkår = Vilkår.SYKEPENGER,
            kilde = "Infotrygd/Speil",
            fom = null,
            tom = null,
            utfall = Utfall.IKKE_IMPLEMENTERT,
            detaljer = "",
        )
    }
}
