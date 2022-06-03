package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.domene.fakta.Faktum
import no.nav.tiltakspenger.domene.fakta.Fakta
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import kotlin.reflect.KClass

data class Vilkårsvurdering<FaktaType : Fakta<FaktumType>, FaktumType: Faktum> (
    val vilkår: Vilkår<FaktaType>,
    val fakta: FaktaType,
    val vurderingsperiode: Periode,
    val utfallsperioder: List<Utfallsperiode> = listOf(
        Utfallsperiode(utfall = Utfall.IkkeVurdert, periode = vurderingsperiode)
    ),
    private val faktumType: KClass<FaktumType>
) {

    companion object {
        inline operator fun <FaktaType : Fakta<FaktumType>, reified FaktumType: Faktum>invoke(
            vilkår: Vilkår<FaktaType>,
            fakta: FaktaType,
            vurderingsperiode: Periode,
            utfallsperioder: List<Utfallsperiode> = listOf(
                Utfallsperiode(utfall = Utfall.IkkeVurdert, periode = vurderingsperiode)
            ),
        ): Vilkårsvurdering<FaktaType, FaktumType> {
            return Vilkårsvurdering(
                vilkår = vilkår,
                fakta = fakta,
                vurderingsperiode = vurderingsperiode,
                utfallsperioder = utfallsperioder,
                faktumType = FaktumType::class,
            )
        }
    }

    fun vurder(faktum: Faktum): Vilkårsvurdering<FaktaType, FaktumType> {
        if (!faktumType.isInstance(faktum)) return this
        val oppdaterteFakta: FaktaType = fakta.leggTil(faktum as FaktumType) as FaktaType
        return this.copy(
            utfallsperioder = vilkår.vurder(oppdaterteFakta, vurderingsperiode),
            fakta = oppdaterteFakta,
        )
    }
}
