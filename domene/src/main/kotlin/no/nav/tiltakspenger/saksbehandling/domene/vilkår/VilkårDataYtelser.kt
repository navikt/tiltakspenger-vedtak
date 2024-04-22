package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.dekkerHele
import no.nav.tiltakspenger.felles.erInnenfor
import no.nav.tiltakspenger.felles.inneholderOverlapp
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.*
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde.TILTAKSPENGER_VEDTAK

data class VilkårDataYtelser(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysningerSaksbehandler: List<YtelseSaksopplysning>,
    val saksopplysningerAnnet: List<YtelseSaksopplysning>,
    val vurderinger: List<Vurdering>,
) {

    fun ikkeInnhentet(): Boolean {
        return (saksopplysningerSaksbehandler.isEmpty() && saksopplysningerAnnet.isEmpty())
    }

    fun harEttUniktVilkår(): Boolean {
        return (saksopplysningerSaksbehandler.all { it.vilkår == vilkår }
            && saksopplysningerAnnet.all { it.vilkår == vilkår })
    }

    init {
        check(harEttUniktVilkår()) { "Kan ikke vilkårsvurdere saksopplysninger med forskjellige vilkår" }

        check(!saksopplysningerSaksbehandler.map { it.periode }.inneholderOverlapp()) {
            "Ulike saksopplysninger for samme vilkår kan ikke ha overlappende perioder"
        }

        check(saksopplysningerSaksbehandler.map { it.periode }.dekkerHele(vurderingsperiode)) {
            "Vi må ha saksopplysninger for hele vurderingsperioden for å kunne vurdere vilkåret"
        }

        check(saksopplysningerSaksbehandler.map { it.periode }.erInnenfor(vurderingsperiode)) {
            "Vi kan ikke vilkårsvurdere saksopplysninger som går utenfor vurderingsperioden"
        }
    }

    fun leggTilSaksopplysning(saksopplysninger: List<YtelseSaksopplysning>): VilkårDataYtelser {
        val kilde = saksopplysninger.first().kilde

        return if (kilde == Kilde.SAKSB) {
            this.copy(
                saksopplysningerSaksbehandler = saksopplysninger,
            )

        } else {
            this.copy(
                saksopplysningerAnnet = saksopplysninger,
            )
        }
    }

    fun avklarFakta(): List<YtelseSaksopplysning> {
        if (saksopplysningerSaksbehandler.isEmpty() && saksopplysningerAnnet.isEmpty()) {
            throw IllegalStateException("Kan ikke avklare fakta uten noen saksopplysninger")
        }

        return saksopplysningerSaksbehandler.ifEmpty { saksopplysningerAnnet }
    }

    fun vilkårsvurder(): VilkårDataYtelser {
        val avklarteFakta = avklarFakta()

        if (avklarteFakta.isEmpty()) {
            return this.copy(vurderinger = listOf(Vurdering(vilkår = vilkår,  kilde = TILTAKSPENGER_VEDTAK, )))
        }

        require(avklarteFakta.isNotEmpty()) { "Må ha avklarte fakta for å vilkårsvurdere" }

        val vurderinger = avklarteFakta.map { saksopplysning -> saksopplysning.vilkårsvurder(vurderingsperiode) }

        return this.copy(
            vurderinger = vurderinger,
        )
    }
}

