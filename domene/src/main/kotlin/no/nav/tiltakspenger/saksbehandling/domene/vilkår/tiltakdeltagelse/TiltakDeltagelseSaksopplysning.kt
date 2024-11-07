package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface TiltakDeltagelseSaksopplysning {
    val tiltakNavn: String
    val eksternId: String
    val kilde: Tiltakskilde
    val deltagelsePeriode: Periode
    val girRett: Boolean
    val status: TiltakDeltakerstatus
    val tidsstempel: LocalDateTime
    val tiltakstype: TiltakstypeSomGirRett
    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: Saksbehandler?

    data class Register(
        override val tiltakNavn: String,
        override val eksternId: String,
        override val tidsstempel: LocalDateTime,
        override val deltagelsePeriode: Periode,
        override val girRett: Boolean,
        override val status: TiltakDeltakerstatus,
        override val kilde: Tiltakskilde,
        override val tiltakstype: TiltakstypeSomGirRett,
    ) : TiltakDeltagelseSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null
    }
}
