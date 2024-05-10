package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.SaksopplysningInterface
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TiltakSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import java.time.LocalDate

data class VilkårData(
    val ytelse: YtelseVilkår,
    val tiltak: TiltakVilkår,
//    val barn: List<>,
//    val alder: List<>,
//    val søknadstidspunkt: List<>
) {
    companion object {
        fun opprettFraSøknad(søknad: Søknad): VilkårData {
            return VilkårData(
                ytelse = YtelseVilkår.opprettFraSøknad(søknad),
                tiltak = TiltakVilkår.opprettFraSøknad(søknad),
            )
        }

        fun tempKompileringsDemp(vurderingsperiode: Periode = Periode(LocalDate.MIN, LocalDate.MAX)): VilkårData {
            return VilkårData(
                ytelse = YtelseVilkår.tempKompileringsDemp(vurderingsperiode),
                tiltak = TiltakVilkår.tempKompileringsDemp(vurderingsperiode),
            )
        }
    }

    fun leggTilSøknad(søknad: Søknad): VilkårData {
        val ytelse = ytelse.leggTilSøknad(søknad)

        return this.copy(
            ytelse = ytelse,
        )
    }

    fun vilkårsvurder(): VilkårData {
        return this.copy(
            ytelse = ytelse.vilkårsvurder(),
            tiltak = tiltak.vilkårsvurder(),
        )
    }

    fun vilkårsvurderBarn(): VilkårData {
        return this // TODO!!!
    }

    fun vurderinger(): List<Vurdering> {
        return ytelse.vurderinger() // + vilkårsvurderBarn()
    }

    fun leggTilSaksopplysning(saksopplysning: SaksopplysningInterface): VilkårData {
        val vilkår = saksopplysning.vilkår
        return if (vilkår in YtelseVilkår.ytelser()) {
            this.copy(ytelse = ytelse.leggTilSaksopplysning(saksopplysning as YtelseSaksopplysning))
        } else if (vilkår == Vilkår.TILTAKDELTAKELSE) {
            this.copy(tiltak = tiltak.leggTilSaksopplysning(saksopplysning as TiltakSaksopplysning))
        } else {
            throw IllegalArgumentException("Kan ikke legge til saksopplysning for $vilkår")
        }
    }
}
