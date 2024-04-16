package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.*

data class OppfyllbarVilkårData(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysningerSaksbehandler: List<SaksopplysningInterface>,
    val saksopplysningerAnnet: List<SaksopplysningInterface>,
    // TODO Kommentar:
    // Når skal avklarteFakta oppdateres? Kunne avklarFakta() bare vært en funksjon som
    // alltid sa hvordan de resulterende faktaene så ut basert på saksopplysningene fra SBH og annet?
    val vurderinger: List<Vurdering>,
) {
    companion object {
        fun initOppfyltVilkårData(vilkår: Vilkår, vurderingsperiode: Periode) =
            OppfyllbarVilkårData(
                vilkår = vilkår,
                vurderingsperiode = vurderingsperiode,
                saksopplysningerSaksbehandler = emptyList(),
                saksopplysningerAnnet = emptyList(),
                vurderinger = emptyList(),
            )
    }

    fun leggTilSaksopplysning(saksopplysning: SaksopplysningInterface): OppfyllbarVilkårData {
        if (saksopplysning.kilde == Kilde.SAKSB) {
            val gamleSaksopplysninger = saksopplysningerSaksbehandler ?: emptyList()
            return this.copy(
                saksopplysningerSaksbehandler = gamleSaksopplysninger + saksopplysning,
            )

        } else {
            val gamleSaksopplysninger = saksopplysningerAnnet ?: emptyList()
            return this.copy(
                saksopplysningerAnnet = gamleSaksopplysninger + saksopplysning,
            )
        }
    }

    fun avklarFakta(): List<SaksopplysningInterface> {
        if (saksopplysningerSaksbehandler.isEmpty() && saksopplysningerAnnet.isEmpty()) {
            throw IllegalStateException("Kan ikke avklare fakta uten noen saksopplysninger")
        }

        return saksopplysningerSaksbehandler.ifEmpty { saksopplysningerAnnet }
    }

    fun vilkårsvurder(): OppfyllbarVilkårData {
        val avklarteFakta = avklarFakta()
        require(avklarteFakta.isNotEmpty()) { "Må ha avklarte fakta for å vilkårsvurdere" }

        val vurderinger = when (this.vilkår) {
            in  YtelseSaksopplysning.YTELSESVILKÅR -> {
                avklarteFakta.filterIsInstance<YtelseSaksopplysning>().vilkårsvurder(vurderingsperiode)
            }
            is Vilkår.ALDER -> {
                avklarteFakta.filterIsInstance<AlderSaksopplysning>().vilkårsvurder(vurderingsperiode)
            }
            is Vilkår.BARNETILLEGG -> {
                avklarteFakta.filterIsInstance<BarnSaksopplysning>().vilkårsvurder(vurderingsperiode)
            }
            is Vilkår.SØKNADSFRIST -> {
                avklarteFakta.filterIsInstance<SøknadTidspunktSaksopplysning>().vilkårsvurder(vurderingsperiode)
            }
            else -> {
                throw IllegalStateException("Kan ikke vilkårsvurdere vilkår $vilkår")
            }
        }

        return this.copy(
            vurderinger = vurderinger,
        )
    }
}

