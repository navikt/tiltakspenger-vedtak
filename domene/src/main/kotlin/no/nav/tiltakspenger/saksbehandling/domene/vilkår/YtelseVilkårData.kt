package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.dekkerHele
import no.nav.tiltakspenger.felles.erInnenfor
import no.nav.tiltakspenger.felles.inneholderOverlapp
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.*
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde.TILTAKSPENGER_VEDTAK

data class YtelseVilkårData(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysningerSaksbehandler: List<YtelseSaksopplysning>,
    val saksopplysningerAnnet: List<YtelseSaksopplysning>,
    val avklarteSaksopplysninger: List<YtelseSaksopplysning>,
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
        require(harEttUniktVilkår()) { "Kan ikke vilkårsvurdere saksopplysninger med forskjellige vilkår" }

        require(!saksopplysningerSaksbehandler.map { it.periode }.inneholderOverlapp()) {
            "Ulike saksopplysninger for samme vilkår kan ikke ha overlappende perioder"
        }

        require(saksopplysningerSaksbehandler.map { it.periode }.dekkerHele(vurderingsperiode)) { // Misvisende metodenavn
            "Vi må ha saksopplysninger for hele vurderingsperioden for å kunne vurdere vilkåret"
        }

        require(saksopplysningerSaksbehandler.map { it.periode }.erInnenfor(vurderingsperiode)) {
            "Vi kan ikke vilkårsvurdere saksopplysninger som går utenfor vurderingsperioden"
        }
    }

    fun leggTilSaksopplysning(saksopplysninger: List<YtelseSaksopplysning>): YtelseVilkårData {
        val kilde = saksopplysninger.first().kilde

        return if (kilde == Kilde.SAKSB) {
            this.copy(
                saksopplysningerSaksbehandler = saksopplysninger,
            )

        } else {
            this.copy(
                saksopplysningerAnnet = saksopplysninger,
            )
        }.avklarFakta()
    }

    fun avklarFakta(): YtelseVilkårData {
        if (saksopplysningerSaksbehandler.isEmpty() && saksopplysningerAnnet.isEmpty()) {
            emptyList<YtelseSaksopplysning>()
        }

        return this.copy(
            avklarteSaksopplysninger = saksopplysningerSaksbehandler.ifEmpty { saksopplysningerAnnet }
        )
    }

    fun vilkårsvurder(): YtelseVilkårData {
        if (avklarteSaksopplysninger.isEmpty()) {
            return this.copy(
                vurderinger = listOf(
                    Vurdering(
                        vilkår = vilkår,
                        kilde = TILTAKSPENGER_VEDTAK,
                        vurderingsperiode.fra,
                        vurderingsperiode.til,
                        Utfall.KREVER_MANUELL_VURDERING,
                        "",
                    ),
                ),
            )
        }

        require(avklarteSaksopplysninger.isNotEmpty()) { "Må ha avklarte fakta for å vilkårsvurdere" }

        val vurderinger = avklarteSaksopplysninger.map { saksopplysning -> saksopplysning.vilkårsvurder(vurderingsperiode) }

        return this.copy(
            vurderinger = vurderinger,
        )
    }
}

