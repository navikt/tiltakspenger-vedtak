package no.nav.tiltakspenger.domene.vilkår.temp

sealed interface VilkårBehandling

class TrengerViDenneFasenForVilkårBehandling(
    private val vilkårData: VilkårData,
) : VilkårBehandling {
    fun leggTilSaksopplysning(saksopplysning: PeriodeMedVerdi<Saksopplysning>): InnhentetSaksopplysningerForVilkårBehandling {
        return InnhentetSaksopplysningerForVilkårBehandling(
            vilkårData.copy(
                saksopplysninger = vilkårData.saksopplysninger.leggTilSaksopplysning(saksopplysning),
            ),
        )
    }
}

class InnhentetSaksopplysningerForVilkårBehandling(
    private val vilkårData: VilkårData,
) : VilkårBehandling {
    fun leggTilSaksopplysning(saksopplysning: PeriodeMedVerdi<Saksopplysning>): InnhentetSaksopplysningerForVilkårBehandling {
        return InnhentetSaksopplysningerForVilkårBehandling(
            vilkårData.copy(saksopplysninger = vilkårData.saksopplysninger.leggTilSaksopplysning(saksopplysning)),
        )
    }

    fun avklarFakta(): AvklartFaktaForVilkårBehandling {
        return AvklartFaktaForVilkårBehandling(
            vilkårData.copy(
                avklartFakta = vilkårData.saksopplysninger.avklarFakta(),
            ),
        )
    }
}

class AvklartFaktaForVilkårBehandling(
    private val vilkårData: VilkårData,
) : VilkårBehandling {
    fun vurder(): VilkårsvurdertVilkårBehandling {
        return VilkårsvurdertVilkårBehandling(
            vilkårData.copy(
                vurderinger = vilkårData.avklartFakta.lagVurdering(),
            ),
        )
    }
}

class VilkårsvurdertVilkårBehandling(
    private val vilkårData: VilkårData,
) : VilkårBehandling {
    fun vurderinger(): PeriodeMedVurderinger = vilkårData.vurderinger
}
