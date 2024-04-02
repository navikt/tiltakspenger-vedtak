package no.nav.tiltakspenger.domene.vilkår.temp

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.domene.vilkår.Utfall
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import org.junit.jupiter.api.Test

class VilkårBehandlingTest {

    @Test
    fun test1() {
        val vurderingsperiode = Periode(1.januar(2023), 1.mars(2023))
        val vilkårBehandling = TrengerViDenneFasenForVilkårBehandling(
            vilkårData = VilkårData(Vilkår.AAP, vurderingsperiode),
        )
        val vilkårsvurdert: VilkårsvurdertVilkårBehandling = vilkårBehandling.leggTilSaksopplysning(
            PeriodeMedVerdi(
                SaksopplysningFraSaksbehandler(
                    Vilkår.AAP,
                    TypeSaksopplysning.HAR_YTELSE,
                ),
                Periode(1.januar(2023), 10.januar(2023)),
            ),
        )
            .avklarFakta()
            .vurder()
        vilkårsvurdert.vurderinger().periodeMedVerdier.perioder() shouldContainExactlyInAnyOrder
            listOf(
                PeriodeMedVerdi(
                    Vurdering(Vilkår.AAP, Kilde.SAKSB, Utfall.IKKE_OPPFYLT),
                    Periode(1.januar(2023), 10.januar(2023)),
                ),
                PeriodeMedVerdi(
                    Vurdering(Vilkår.AAP, Kilde.SAKSB, Utfall.KREVER_MANUELL_VURDERING),
                    Periode(11.januar(2023), 1.mars(2023)),
                ),
            )
    }
}
