package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.SaksopplysningInterface
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import java.time.LocalDate

data class VilkårData(
    val ytelse: VilkårYtelser,
//    val barn: List<>,
//    val alder: List<>,
//    val tiltak: List<>,
//    val søknadstidspunkt: List<>
) {
    companion object {
        fun opprettFraSøknad(søknad: Søknad): VilkårData {
            return VilkårData(
                ytelse = VilkårYtelser.opprettFraSøknad(søknad)
            )
        }

        fun tempKompileringsDemp(vurderingsperiode: Periode = Periode(LocalDate.MIN, LocalDate.MAX)): VilkårData {
            return VilkårData(
                ytelse = VilkårYtelser.tempKompileringsDemp(vurderingsperiode)
            )
        }
    }

    fun leggTilSøknad(søknad: Søknad): VilkårData {
        val ytelse = ytelse.leggTilSøknad(søknad)

        return this.copy(
            ytelse = ytelse
        )
    }

    fun avklarFakta(): List<SaksopplysningInterface> {
        return ytelse.avklarFakta()
    }

    fun vilkårsvurder(): List<Vurdering> {
        return ytelse.vilkårsvurder()
    }

    fun vilkårsvurderBarn(): List<Vurdering> {
        return emptyList()
    }

    fun vurderinger(): List<Vurdering> {
        return vilkårsvurder() + vilkårsvurderBarn()
    }

    fun leggTilSaksopplysning(saksopplysning: List<SaksopplysningInterface>): VilkårData {
        val vilkår = saksopplysning.first().vilkår
        return if (vilkår in VilkårYtelser.ytelser()) {
            this.copy(ytelse = ytelse.leggTilSaksopplysning(saksopplysning as List<YtelseSaksopplysning>))
        } else {
            throw IllegalArgumentException("Kan ikke legge til saksopplysning for $vilkår")
        }
        return this
    }

}
