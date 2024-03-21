package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger

data class Inngangsvilkår(
    val vurderingsperiode: Periode,
    val vilkårsvurderinger: List<Vilkårsvurdering>,
) {
    companion object {
        operator fun invoke(vurderingsperiode: Periode): Inngangsvilkår {
            return Inngangsvilkår(
                vurderingsperiode = vurderingsperiode,
                vilkårsvurderinger = listOf(
                    Vilkårsvurdering(Vilkår.DAGPENGER, vurderingsperiode),
                    Vilkårsvurdering(Vilkår.AAP, vurderingsperiode),
                ),
            )
        }
    }

    fun leggTilSøknad(søknad: Søknad): Inngangsvilkår {
        val nyeSaksopplysninger = Saksopplysninger.lagSaksopplysningerAvSøknad(søknad)
        val nyeVilkårsvurderinger = this.vilkårsvurderinger.map { vilkårsvurdering ->
            vilkårsvurdering.leggTilSaksopplysninger(nyeSaksopplysninger)
        }
        return this.copy(
            vilkårsvurderinger = nyeVilkårsvurderinger,
        )
    }
}
/*
data class SaksopplysningPeriode(
    val periode: Periode,
    val kilde: Kilde,
    val vilkår: Vilkår,
    val detaljer: String,
    val typeSaksopplysning: TypeSaksopplysning,
    val saksbehandler: String? = null,
)

 */

data class Vilkårsvurdering private constructor(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysningerFraSøknad: List<Saksopplysning>,
    val saksopplysningerFraRegistre: List<Saksopplysning>,
    val saksopplysningerFraSBH: List<Saksopplysning>,
    val vurderinger: List<Vurdering>,
) {

    companion object {
        operator fun invoke(vilkår: Vilkår, vurderingsperiode: Periode) = Vilkårsvurdering(
            vilkår = vilkår,
            vurderingsperiode = vurderingsperiode,
            saksopplysningerFraSøknad = emptyList(),
            saksopplysningerFraRegistre = emptyList(),
            saksopplysningerFraSBH = emptyList(),
            vurderinger = emptyList(),
        )
    }

    fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Vilkårsvurdering {
        if (saksopplysning.vilkår == vilkår) {
            return when (saksopplysning.kilde) {
                Kilde.SAKSB ->
                    this.copy(saksopplysningerFraSBH = saksopplysningerFraSBH + saksopplysning)

                Kilde.SØKNAD ->
                    this.copy(saksopplysningerFraSøknad = saksopplysningerFraSøknad + saksopplysning)

                else -> this.copy(saksopplysningerFraRegistre = saksopplysningerFraRegistre + saksopplysning)
            }
        }
        return this
    }

    fun leggTilSaksopplysninger(saksopplysninger: List<Saksopplysning>): Vilkårsvurdering =
        saksopplysninger.fold(this) { vilkårsvurdering, saksopplysning ->
            vilkårsvurdering.leggTilSaksopplysning(saksopplysning)
        }

    fun leggTilSøknad(søknad: Søknad) {
        this.copy(saksopplysningerFraSøknad = Saksopplysninger.lagSaksopplysningerAvSøknad(søknad))
    }

    fun vilkårsvurder(vurderingsperiode: Periode): Vilkårsvurdering =
        this.copy(
            vurderinger = this.saksopplysninger().flatMap {
                it.lagVurdering(vurderingsperiode)
            },
        )

    private fun saksopplysninger(): List<Saksopplysning> =
        this.saksopplysningerFraSøknad + this.saksopplysningerFraRegistre + this.saksopplysningerFraSBH
}
