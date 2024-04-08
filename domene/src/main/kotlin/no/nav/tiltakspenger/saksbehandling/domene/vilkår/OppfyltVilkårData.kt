package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.erInnenfor
import no.nav.tiltakspenger.felles.erSammenhengende
import no.nav.tiltakspenger.felles.inneholderOverlapp
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import java.time.LocalDateTime

data class OppfyltVilkårData(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysningerSaksbehandler: YtelseSaksopplysningerForEnKilde,
    val saksopplysningerAnnet: YtelseSaksopplysningerForEnKilde,
    val avklarteFakta: YtelseSaksopplysningerForEnKilde,
    val vurderinger: List<Vurdering>,
) {
    fun leggTilSaksopplysning(saksopplysning: Saksopplysning) {
        // TODO Her må vi enten få inn hele sannheten eller legge denne ene til i listen og lage de implisitte
    }

    fun avklarFakta(): OppfyltVilkårData {
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
