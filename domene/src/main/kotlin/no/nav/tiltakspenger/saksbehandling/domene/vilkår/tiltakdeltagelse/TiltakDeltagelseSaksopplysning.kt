package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface TiltakDeltagelseSaksopplysning {
    val tiltakNavn: String
    val kilde: String
    val deltagelsePeriode: Periode
    val girRett: Boolean
    val status: String
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?

    data class Tiltak(
        override val tiltakNavn: String,
        override val tidsstempel: LocalDateTime,
        override val deltagelsePeriode: Periode,
        override val girRett: Boolean,
        override val status: String,
        override val kilde: String,
    ) : TiltakDeltagelseSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null
    }
}
