package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TiltakSaksopplysning

data class TiltakVilkårData(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysningerSaksbehandler: TiltakSaksopplysning?,
    val saksopplysningerAnnet: TiltakSaksopplysning?,
    val avklarteSaksopplysninger: TiltakSaksopplysning?,
    val vurderinger: List<Vurdering>,
) {
    fun leggTilSaksopplysning(tiltakSaksopplysning: TiltakSaksopplysning): TiltakVilkårData {
        val kilde = tiltakSaksopplysning.kilde

        return if (kilde == Kilde.SAKSB) {
            this.copy(
                saksopplysningerSaksbehandler = tiltakSaksopplysning,
            )
        } else {
            this.copy(
                saksopplysningerAnnet = tiltakSaksopplysning,
            )
        }.avklarFakta()
    }

    fun harSaksopplysninger(): Boolean { // todo: finn ut hva man vil med denne og endre navngivning ++
        return (saksopplysningerSaksbehandler != null || saksopplysningerAnnet != null)
    }

    private fun måHaSaksopplysninger() {
        require(saksopplysningerSaksbehandler != null || saksopplysningerAnnet != null) { "Må ha saksopplysninger" }
    }

    private fun avklarFakta(): TiltakVilkårData {
        if (!harSaksopplysninger()) {
            emptyList<TiltakSaksopplysning>()
        }

        måHaSaksopplysninger()

        return this.copy(
            avklarteSaksopplysninger = saksopplysningerSaksbehandler ?: saksopplysningerAnnet,
        )
    }

    fun vilkårsvurder(): TiltakVilkårData {
        if (avklarteSaksopplysninger == null) {
            return this.copy(
                vurderinger = listOf(
                    Vurdering(
                        vilkår = vilkår,
                        kilde = Kilde.TILTAKSPENGER_VEDTAK,
                        vurderingsperiode.fra,
                        vurderingsperiode.til,
                        Utfall.KREVER_MANUELL_VURDERING,
                        "",
                    ),
                ),
            )
        }

        val vurdering = avklarteSaksopplysninger.vilkårsvurder()

        return this.copy(
            vurderinger = vurderinger + vurdering,
        )
    }
}
