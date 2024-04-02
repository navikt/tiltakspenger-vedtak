package no.nav.tiltakspenger.domene.vilkår.temp

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import org.junit.jupiter.api.Test

class SammenlignbarTest {

    @Test
    fun testWrappere() {
        val wrappedLong1 = 1L.wrap()
        val wrappedLong2 = 13L.wrap()
        val wrappedLong3 = 13L.wrap()
        val wrappedString1 = "foo".wrap()
        val wrappedString2 = "bar".wrap()
        listOf(wrappedLong3, wrappedLong1, wrappedLong2).distinctBy { it.sammenlignbareFelter() } shouldBe listOf(
            wrappedLong3,
            wrappedLong1,
        )
        wrappedLong3.erLik(wrappedLong1) shouldBe false
        wrappedLong3.erLik(wrappedLong2) shouldBe true

        listOf(
            wrappedLong3,
            wrappedLong1,
            wrappedLong2,
            wrappedString1,
            wrappedString2,
            wrappedString2,
        ).distinctBy { it.sammenlignbareFelter() } shouldBe listOf(
            wrappedLong3,
            wrappedLong1,
            wrappedString1,
            wrappedString2,
        )
        wrappedLong3.erLik(wrappedLong1) shouldBe false
        wrappedLong3.erLik(wrappedLong2) shouldBe true
        wrappedString1.erLik(wrappedString1) shouldBe true
        wrappedString1.erLik(wrappedString2) shouldBe false
    }

    @Test
    fun testVurdering() {
        val vurdering1 = Vurdering(
            vilkår = Vilkår.AAP,
            kilde = Kilde.SAKSB,
            utfall = Utfall.OPPFYLT,
        )
        val vurdering2 = Vurdering(
            vilkår = Vilkår.AAP,
            kilde = Kilde.SAKSB,
            utfall = Utfall.OPPFYLT,
        )
        val vurdering3 = Vurdering(
            vilkår = Vilkår.AAP,
            kilde = Kilde.SØKNAD,
            utfall = Utfall.OPPFYLT,
        )
        vurdering1.erLik(vurdering2) shouldBe true
        vurdering1.erLik(vurdering3) shouldBe false
        listOf(vurdering1, vurdering2, vurdering3).distinctBy { it.sammenlignbareFelter() } shouldBe listOf(
            vurdering1,
            vurdering3,
        )
    }
}
