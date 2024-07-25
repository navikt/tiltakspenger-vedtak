package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import arrow.core.Either
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.totalePeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.LeggTilAlderSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.LeggTilIntroSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.LeggTilKravfristSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KVPVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.LeggTilKvpSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår.PeriodenMåVæreLikVurderingsperioden
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak.LeggTilTiltakSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak.TiltakVilkår

/**
 * Ref til begrepskatalogen.
 * Vil både være inngangsvilkår og andre vilkår.
 * Det totale settet vilkår.
 */
data class Vilkårssett(
    // TODO jah: saksopplysninger, vilkårsvurderinger og kravdatoSaksopplysninger, utfallsperioder flyttes gradvis til hvert sitt vilkår. Og slettes når vilkår 2.0 er ferdig.
    val saksopplysninger: List<Saksopplysning>,
    val vilkårsvurderinger: List<Vurdering>,
    val utfallsperioder: List<Utfallsperiode>,
    val institusjonsoppholdVilkår: InstitusjonsoppholdVilkår,
    val kvpVilkår: KVPVilkår,
    val tiltakVilkår: TiltakVilkår,
    val introVilkår: IntroVilkår,
    val livsoppholdVilkår: LivsoppholdVilkår,
    val alderVilkår: AlderVilkår,
    val kravfristVilkår: KravfristVilkår,
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

    fun oppdaterIntro(command: LeggTilIntroSaksopplysningCommand): Vilkårssett {
        return this.copy(
            introVilkår = introVilkår.leggTilSaksbehandlerSaksopplysning(command),
        )
    }

    fun oppdaterAlder(command: LeggTilAlderSaksopplysningCommand): Vilkårssett {
        return this.copy(
            alderVilkår = alderVilkår.leggTilSaksbehandlerSaksopplysning(command),
        )
    }

    fun oppdaterKravdato(command: LeggTilKravfristSaksopplysningCommand): Vilkårssett {
        return this.copy(
            kravfristVilkår = kravfristVilkår.leggTilSaksbehandlerSaksopplysning(command),
        )
    }

    fun oppdaterTiltak(command: LeggTilTiltakSaksopplysningCommand): Vilkårssett {
        return this.copy(
            tiltakVilkår = tiltakVilkår.leggTilSaksbehandlerSaksopplysning(command),
        )
    }

    fun oppdaterLivsopphold(command: LeggTilLivsoppholdSaksopplysningCommand): Either<PeriodenMåVæreLikVurderingsperioden, Vilkårssett> {
        return livsoppholdVilkår.leggTilSaksbehandlerSaksopplysning(command).map { this.copy(livsoppholdVilkår = it) }
    }
}
