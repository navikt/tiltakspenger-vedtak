package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.totalePeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KVPVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.LeggTilKvpSaksopplysningCommand

/**
 * Ref til begrepskatalogen.
 * Vil både være inngangsvilkår og andre vilkår.
 * Det totale settet vilkår.
 */
data class Vilkårssett(
    // TODO jah: saksopplysninger, vilkårsvurderinger og kravdatoSaksopplysninger, utfallsperioder flyttes gradvis til hvert sitt vilkår. Og slettes når vilkår 2.0 er ferdig.
    val saksopplysninger: List<Saksopplysning>,
    val vilkårsvurderinger: List<Vurdering>,
    val kravdatoSaksopplysninger: KravdatoSaksopplysninger,
    val utfallsperioder: List<Utfallsperiode>,
    val institusjonsoppholdVilkår: InstitusjonsoppholdVilkår,
    val kvpVilkår: KVPVilkår,
) {
    val totalePeriode = kvpVilkår.totalePeriode

    init {
        // TODO jah: F.eks. et tiltak kan strekke seg på utsiden av vurderingsperioden?. Bør legges inn når vi er ferdig med vilkår 2.0
//        if (vilkårsvurderinger.totalePeriode() != null) {
//            require(kvpVilkår.totalePeriode == vilkårsvurderinger.totalePeriode()) {
//                "KVPVilkår (${kvpVilkår.totalePeriode}) og vilkårsvurderinger (${vilkårsvurderinger.totalePeriode()}) sine perioder må være like."
//            }
//        }
        // TODO jah: Brekker for mange tester ved å legge inn den sjekken her. Bør legges inn når vi er ferdig med vilkår 2.0
        require(kvpVilkår.totalePeriode.inneholderHele(saksopplysninger.totalePeriode())) {
            "KVPVilkår (${kvpVilkår.totalePeriode}) og saksopplysninger (${saksopplysninger.totalePeriode()}) sine perioder må være like."
        }
    }

    fun oppdaterSaksopplysninger(saksopplysninger: List<Saksopplysning>): Vilkårssett {
        return this.copy(saksopplysninger = saksopplysninger)
    }

    fun oppdaterSaksopplysning(saksopplysning: Saksopplysning): Vilkårssett {
        return this.copy(saksopplysninger = saksopplysninger.oppdaterSaksopplysninger(saksopplysning))
    }

    fun oppdaterVilkårsvurderinger(
        vilkårsvurderinger: List<Vurdering>,
        utfallsperioder: List<Utfallsperiode>,
    ): Vilkårssett {
        return this.copy(
            vilkårsvurderinger = vilkårsvurderinger,
            utfallsperioder = utfallsperioder,
        )
    }

    fun vurderingsperiodeEndret(nyVurderingsperiode: Periode): Vilkårssett {
        // TODO: "Saksopplysninger fra registre må hentes inn på nytt, saksopplysninger fra søknad må paddes med UAVKLART, saksopplysninger fra saksbehandler må enten paddes eller slettes."
        return this
    }

    fun oppdaterKVP(command: LeggTilKvpSaksopplysningCommand): Vilkårssett {
        return this.copy(
            kvpVilkår = kvpVilkår.leggTilSaksbehandlerSaksopplysning(command),
        )
    }
}
