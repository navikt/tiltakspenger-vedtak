package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde.TILTAKSPENGER_VEDTAK
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning

data class YtelseVilkårData(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysningerSaksbehandler: YtelseSaksopplysning?,
    val saksopplysningerAnnet: YtelseSaksopplysning?,
    val avklarteSaksopplysninger: YtelseSaksopplysning?,
    val vurderinger: List<Vurdering>,
) {

    private fun måHaSaksopplysninger() {
        require(saksopplysningerSaksbehandler != null || saksopplysningerAnnet != null) { "Må ha saksopplysninger" }
    }

    fun harSaksopplysninger(): Boolean { // todo: finn ut hva man vil med denne og endre navngivning ++
        return (saksopplysningerSaksbehandler != null || saksopplysningerAnnet != null)
    }

    fun harEttUniktVilkår(): Boolean {
        return if (saksopplysningerSaksbehandler != null) {
            saksopplysningerSaksbehandler.vilkår == vilkår
        } else if (saksopplysningerAnnet != null) {
            saksopplysningerAnnet.vilkår == vilkår
        } else {
            false
        }
    }

    init {
        if (harSaksopplysninger()) {
            require(harEttUniktVilkår()) { "Kan ikke vilkårsvurdere saksopplysninger med forskjellige vilkår" }
        }

        if (saksopplysningerSaksbehandler != null) {
            require(!saksopplysningerSaksbehandler.inneholderOverlapp()) {
                "Ulike saksopplysninger for samme vilkår kan ikke ha overlappende perioder"
            }

            require(saksopplysningerSaksbehandler.dekkerHele(vurderingsperiode)) { // Misvisende metodenavn
                "Vi må ha saksopplysninger for hele vurderingsperioden for å kunne vurdere vilkåret"
            }

            require(saksopplysningerSaksbehandler.erInnenfor(vurderingsperiode)) {
                "Vi kan ikke vilkårsvurdere saksopplysninger som går utenfor vurderingsperioden"
            }
        }
    }

    fun leggTilSaksopplysning(saksopplysninger: YtelseSaksopplysning): YtelseVilkårData {
        val kilde = saksopplysninger.kilde

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
        if (!harSaksopplysninger()) {
            emptyList<YtelseSaksopplysning>()
        }

        måHaSaksopplysninger()

        return this.copy(
            avklarteSaksopplysninger = saksopplysningerSaksbehandler ?: saksopplysningerAnnet,
        )
    }

    fun vilkårsvurder(): YtelseVilkårData {
        if (avklarteSaksopplysninger == null) {
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

        require(!avklarteSaksopplysninger.erTom()) { "Må ha avklarte fakta for å vilkårsvurdere" }

        val vurderinger = avklarteSaksopplysninger.vilkårsvurder()

        return this.copy(
            vurderinger = vurderinger,
        )
    }
}
