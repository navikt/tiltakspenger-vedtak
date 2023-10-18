package no.nav.tiltakspenger.domene.saksopplysning

import io.kotest.matchers.collections.shouldContainAll
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import kotlin.test.Test

internal class SaksopplysningTest {

    @Test
    fun `hvis det finnes ytelse i starten av en vurderingsperiode får man IkkeOppfylt i denne perioden og Oppfylt i resten`() {
        val periode = Periode(fra = 1.januar(2023), til = 31.mars(2023))

        val saksopplysning =
            Saksopplysning(
                fom = 1.januar(2023),
                tom = 31.januar(2023),
                kilde = Kilde.SAKSB,
                vilkår = Vilkår.AAP,
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                saksbehandler = null,
            )

        saksopplysning.lagVurdering(periode) shouldContainAll listOf(
            Vurdering.IkkeOppfylt(
                vilkår = Vilkår.AAP,
                kilde = Kilde.SAKSB,
                detaljer = "",
                fom = 1.januar(2023),
                tom = 31.januar(2023),
            ),
            Vurdering.Oppfylt(
                vilkår = Vilkår.AAP,
                kilde = Kilde.SAKSB,
                detaljer = "",
                fom = 1.februar(2023),
                tom = 31.mars(2023),
            ),
        )
    }
}
