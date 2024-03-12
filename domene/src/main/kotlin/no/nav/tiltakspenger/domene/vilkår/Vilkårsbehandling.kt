package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.Periode


interface BehandletVilkår {
    val vurderingsperiode: Periode
    val vilkår: Vilkår
}

class IngentingGjortForVilkår(
    override val vurderingsperiode: Periode,
    override val vilkår: Vilkår,
) : BehandletVilkår {
    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): InnhentetSaksopplysningerForVilkår {
        return InnhentetSaksopplysningerForVilkår(
            vurderingsperiode = vurderingsperiode,
            vilkår = vilkår,
            saksopplysninger = setOf(saksopplysning),
        )
    }
}

class InnhentetSaksopplysningerForVilkår(
    override val vurderingsperiode: Periode,
    override val vilkår: Vilkår,
    val saksopplysninger: Set<Saksopplysning>,
) : BehandletVilkår {
    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): InnhentetSaksopplysningerForVilkår {
        return InnhentetSaksopplysningerForVilkår(
            vurderingsperiode = vurderingsperiode,
            vilkår = vilkår,
            saksopplysninger = saksopplysninger + saksopplysning,
        )
    }

    fun avklarFakta(): AvklartFaktaForVilkår {
        return AvklartFaktaForVilkår(
            vurderingsperiode = vurderingsperiode,
            vilkår = vilkår,
            saksopplysninger = saksopplysninger,
            avklartFakta = saksopplysninger.first(), //TODO Vi har noen algoritmer for å prioritere SBH over annet
        )
    }
}

class AvklartFaktaForVilkår(
    override val vurderingsperiode: Periode,
    override val vilkår: Vilkår,
    val saksopplysninger: Set<Saksopplysning>,
    val avklartFakta: Saksopplysning,
) : BehandletVilkår {
    fun vurder(): VilkårsvurdertVilkår {
        return VilkårsvurdertVilkår(
            vurderingsperiode = vurderingsperiode,
            vilkår = vilkår,
            saksopplysninger = saksopplysninger,
            avklartFakta = avklartFakta,
            vurderinger = avklartFakta.lagVurdering(vurderingsperiode),
        )
    }
}

class VilkårsvurdertVilkår(
    override val vurderingsperiode: Periode,
    override val vilkår: Vilkår,
    val saksopplysninger: Set<Saksopplysning>,
    val avklartFakta: Saksopplysning,
    val vurderinger: List<Vurdering>,
) : BehandletVilkår
