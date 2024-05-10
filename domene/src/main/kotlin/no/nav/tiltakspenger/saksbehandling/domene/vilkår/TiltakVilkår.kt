package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TiltakSaksopplysning

data class TiltakVilkår(
    val tiltakVilkårData: TiltakVilkårData,
) {
    fun leggTilSaksopplysning(tiltakSaksopplysning: TiltakSaksopplysning): TiltakVilkår {
        return this.copy(
            tiltakVilkårData = tiltakVilkårData.leggTilSaksopplysning(tiltakSaksopplysning),
        )
    }

    fun vilkårsvurder(): TiltakVilkår {
        return this.copy(
            tiltakVilkårData = tiltakVilkårData.vilkårsvurder(),
        )
    }

    companion object {
        fun opprettFraSøknad(søknad: Søknad): TiltakVilkår {
            val vurderingsperiode = søknad.vurderingsperiode()
            return TiltakVilkår(
                tiltakVilkårData = TiltakVilkårData(
                    vilkår = Vilkår.TILTAKDELTAKELSE,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysningerAnnet = null,
                    saksopplysningerSaksbehandler = null,
                    avklarteSaksopplysninger = null,
                    vurderinger = emptyList(),
                ),
            )
        }

        fun tempKompileringsDemp(vurderingsperiode: Periode): TiltakVilkår {
            return TiltakVilkår(
                tiltakVilkårData = TiltakVilkårData(
                    vilkår = Vilkår.TILTAKDELTAKELSE,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysningerSaksbehandler = null,
                    saksopplysningerAnnet = null,
                    avklarteSaksopplysninger = null,
                    vurderinger = emptyList(),
                ),
            )
        }
    }
}
