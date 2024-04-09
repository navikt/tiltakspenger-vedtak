package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.erInnenfor
import no.nav.tiltakspenger.felles.erSammenhengende
import no.nav.tiltakspenger.felles.inneholderOverlapp
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import java.time.LocalDateTime

data class OppfyltVilkårData(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysningerSaksbehandler: YtelseSaksopplysningerForEnKilde?,
    val saksopplysningerAnnet: YtelseSaksopplysningerForEnKilde?,
    // TODO Kommentar:
    // Når skal avklarteFakta oppdateres? Kunne avklarFakta() bare vært en funksjon som
    // alltid sa hvordan de resulterende faktaene så ut basert på saksopplysningene fra SBH og annet?
    val avklarteFakta: YtelseSaksopplysningerForEnKilde?,
    val vurderinger: List<Vurdering>,
) {
    companion object {
        fun initOppfyltVilkårData(vilkår: Vilkår, vurderingsperiode: Periode) =
            OppfyltVilkårData(
                vilkår = vilkår,
                vurderingsperiode = vurderingsperiode,
                saksopplysningerSaksbehandler = null,
                saksopplysningerAnnet = null,
                avklarteFakta = null,
                vurderinger = emptyList(),
            )
    }

    fun leggTilSaksopplysning(saksopplysning: YtelseSaksopplysning): OppfyltVilkårData {
        if (saksopplysning.kilde == Kilde.SAKSB) {
            val gamleSaksopplysninger = saksopplysningerSaksbehandler?.saksopplysninger ?: emptyList()
            return this.copy(
                saksopplysningerSaksbehandler = YtelseSaksopplysningerForEnKilde(
                    kilde = Kilde.SAKSB,
                    periode = Periode(fra = saksopplysning.fom, til = saksopplysning.tom),
                    saksopplysninger = gamleSaksopplysninger + saksopplysning,
                    tidspunkt = LocalDateTime.now(),
                ),
            )
        } else {
            val gamleSaksopplysninger = saksopplysningerAnnet?.saksopplysninger ?: emptyList()
            return this.copy(
                saksopplysningerAnnet = YtelseSaksopplysningerForEnKilde(
                    kilde = saksopplysning.kilde,
                    periode = Periode(fra = saksopplysning.fom, til = saksopplysning.tom),
                    saksopplysninger = gamleSaksopplysninger + saksopplysning,
                    tidspunkt = LocalDateTime.now(),
                ),
            )
        }
    }

    fun avklarFakta(): OppfyltVilkårData {
        if (saksopplysningerSaksbehandler == null && saksopplysningerAnnet == null) {
            throw IllegalStateException("Kan ikke avklare fakta uten noen saksopplysninger")
        }

        return this.copy(
            avklarteFakta = if (saksopplysningerSaksbehandler.erSatt()) saksopplysningerSaksbehandler else saksopplysningerAnnet,
        )
    }

    fun vilkårsvurder(): OppfyltVilkårData {
        val vurderinger = avklarteFakta.saksopplysninger.map { it.lagVurdering() } +
            vurderingsperiode.ikkeOverlappendePerioder(vurderinger.map { it.periode() }).map {
                // TODO lag enten en saksopplysning.lagVurder eller lag Vurdering her
                // TODO avklar foretningsreglene her
                //      hvis utvidet i slutten av perioden og er oppfylt kan man sette oppfylt for perioden?
                //      hvis utvidet i slutten av perioden og er ikke oppfylt setter man til manuell?
                //      er det mulig å endre i starten?
            }

        return this
        // TODO()
//        return this.copy(
//            vurderinger = vurderinger
//        )
    }
}

data class YtelseSaksopplysningerForEnKilde(
    val kilde: Kilde,
    val periode: Periode,
    val saksopplysninger: List<YtelseSaksopplysning>,
    val tidspunkt: LocalDateTime,
) {
    init {
        if (kilde == Kilde.SAKSB) {
            require(saksopplysninger.map { it.periode() }.erInnenfor(periode)) { "Saksopplysninger kan ikke ha periode som er utenfor vurderingsperioden" }

            require(!saksopplysninger.map { it.periode() }.inneholderOverlapp()) { "Saksopplysninger kan ikke overlappe" }

            require(saksopplysninger.map { it.periode() }.erSammenhengende(periode)) { "Saksopplysninger kan ikke ha hull" }
        }
    }

    fun erSatt() = saksopplysninger.isNotEmpty()
}
