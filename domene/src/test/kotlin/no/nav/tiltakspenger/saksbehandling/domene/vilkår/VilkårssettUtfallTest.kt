package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.vurderingsperiode
import org.junit.jupiter.api.Test

internal class VilkårssettUtfallTest {
    @Test
    fun `oppfylt dersom alle vilkår er oppfylt`() {
        ObjectMother.vilkårsett().also {
            it.utfallsperioder() shouldBe
                Periodisering(
                    UtfallForPeriode.OPPFYLT,
                    vurderingsperiode(),
                )
            it.samletUtfall shouldBe SamletUtfall.OPPFYLT
        }
    }

    @Test
    fun `uavklart dersom et vilkår er uavklart`() {
        val vilkårsett =
            ObjectMother.vilkårsett(
                livsoppholdVilkår =
                ObjectMother.livsoppholdVilkår(
                    saksopplysningCommand = null,
                ),
            )
        vilkårsett.utfallsperioder() shouldBe
            Periodisering(
                UtfallForPeriode.UAVKLART,
                vurderingsperiode(),
            )
        vilkårsett.samletUtfall shouldBe SamletUtfall.UAVKLART
    }

    @Test
    fun `IkkeImplementertException dersom avslag`() {
        shouldThrow<IkkeImplementertException> {
            ObjectMother.vilkårsett(
                livsoppholdVilkår =
                ObjectMother.livsoppholdVilkår(
                    harLivsoppholdYtelser = true,
                ),
            )
        }.message shouldBe "Støtter ikke avslag enda."
    }
}
