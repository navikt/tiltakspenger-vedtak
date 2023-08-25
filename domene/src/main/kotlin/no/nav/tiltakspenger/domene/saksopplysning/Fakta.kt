package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import java.time.LocalDate

sealed class Fakta {
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
    ) : Fakta() {
        companion object {
            fun lagFakta(ytelser: List<YtelseSak>?, periode: Periode) =
                DagpengerFaktaHjelper.lagFaktaHjelper(ytelser, periode)
        }
    }

    data class Aap(
        override val fom: LocalDate,
        override val tom: LocalDate,
        override val vilkår: Vilkår,
        override val kilde: String,
        override val detaljer: String,
    ) : Fakta() {
        companion object {
            fun lagFakta(ytelser: List<YtelseSak>?, periode: Periode) =
                AapFaktaHjelper.lagFaktaHjelper(ytelser, periode)
        }
    }
}
