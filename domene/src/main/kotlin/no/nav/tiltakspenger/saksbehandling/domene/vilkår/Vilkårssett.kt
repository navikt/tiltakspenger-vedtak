package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger

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
) {

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
}
