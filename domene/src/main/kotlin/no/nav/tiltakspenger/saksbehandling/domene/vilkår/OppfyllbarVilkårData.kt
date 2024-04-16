package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.*

data class OppfyllbarVilkårData(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysningerSaksbehandler: List<SaksopplysningInterface>?,
    val saksopplysningerAnnet: List<SaksopplysningInterface>?,
    // TODO Kommentar:
    // Når skal avklarteFakta oppdateres? Kunne avklarFakta() bare vært en funksjon som
    // alltid sa hvordan de resulterende faktaene så ut basert på saksopplysningene fra SBH og annet?
    val avklarteFakta: List<SaksopplysningInterface>?,
    val vurderinger: List<Vurdering>,
) {
    companion object {
        fun initOppfyltVilkårData(vilkår: Vilkår, vurderingsperiode: Periode) =
            OppfyllbarVilkårData(
                vilkår = vilkår,
                vurderingsperiode = vurderingsperiode,
                saksopplysningerSaksbehandler = null,
                saksopplysningerAnnet = null,
                avklarteFakta = null,
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

    fun avklarFakta(): OppfyllbarVilkårData {
        if (saksopplysningerSaksbehandler == null && saksopplysningerAnnet == null) {
            throw IllegalStateException("Kan ikke avklare fakta uten noen saksopplysninger")
        }

        return this.copy(
            avklarteFakta = saksopplysningerSaksbehandler ?: saksopplysningerAnnet,
        )
    }

    fun vilkårsvurder(): OppfyllbarVilkårData {
        require(avklarteFakta != null) { "Må ha avklarte fakta for å vilkårsvurdere" }
        val vurderinger = avklarteFakta.saksopplysninger.vilkårsvurder(vurderingsperiode)
//        val vurderinger2 = avklarteFakta.saksopplysninger.vilkårsvurder(vurderingsperiode) +
//            vurderingsperiode.ikkeOverlappendePerioder(vurderinger.map { it.periode() }).map {
//                // TODO lag enten en saksopplysning.lagVurder eller lag Vurdering her
//                // TODO avklar foretningsreglene her
//                //      hvis utvidet i slutten av perioden og er oppfylt kan man sette oppfylt for perioden?
//                //      hvis utvidet i slutten av perioden og er ikke oppfylt setter man til manuell?
//                //      er det mulig å endre i starten?
//            }

        return this.copy(
            vurderinger = vurderinger,
        )
    }
}

