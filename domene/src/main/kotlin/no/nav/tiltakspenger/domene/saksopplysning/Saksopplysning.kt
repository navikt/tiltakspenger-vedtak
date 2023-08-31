package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.time.LocalDate

sealed class Saksopplysning {
    abstract val fom: LocalDate
    abstract val tom: LocalDate
    abstract val vilkår: Vilkår
    abstract val kilde: String
    abstract val detaljer: String

    data class Dagpenger(
        override val fom: LocalDate,
        override val tom: LocalDate,
        override val vilkår: Vilkår,
        override val kilde: String,
        override val detaljer: String,
    ) : Saksopplysning() {
        companion object {
            fun lagFakta(ytelser: List<YtelseSak>?, periode: Periode) =
                DagpengerTolker.tolkeData(ytelser, periode)
        }
    }

    data class Aap(
        override val fom: LocalDate,
        override val tom: LocalDate,
        override val vilkår: Vilkår,
        override val kilde: String,
        override val detaljer: String,
    ) : Saksopplysning() {
        companion object {
            fun lagSaksopplysninger(ytelser: List<YtelseSak>?, periode: Periode) =
                AapTolker.tolkeData(ytelser, periode)
        }
    }
}

fun List<Saksopplysning>.lagVurdering(oppfyltSaksopplysning: Saksopplysning): List<Vurdering> =
    // TODO Her må vi kanskje lage Vurderinger for Oppfylte perioder for at vi skal kunne lage DelvisInnvilget?
    this.map { fakta ->
        Vurdering.IkkeOppfylt(
            vilkår = fakta.vilkår,
            kilde = fakta.kilde,
            fom = fakta.fom,
            tom = fakta.tom,
            detaljer = fakta.detaljer,
        )
    }.ifEmpty {
        listOf(
            Vurdering.Oppfylt(
                vilkår = oppfyltSaksopplysning.vilkår,
                kilde = oppfyltSaksopplysning.kilde,
                detaljer = oppfyltSaksopplysning.detaljer,
            ),
        )
    }
