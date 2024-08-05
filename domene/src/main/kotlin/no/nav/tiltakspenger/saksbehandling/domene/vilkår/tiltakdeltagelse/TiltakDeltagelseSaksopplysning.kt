package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface TiltakDeltagelseSaksopplysning {
    val tiltakNavn: String
    val kilde: Tiltakskilde
    val deltagelsePeriode: Periode
    val girRett: Boolean
    val status: String
    val tidsstempel: LocalDateTime
    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: Saksbehandler?

    data class Register(
        override val tiltakNavn: String,
        override val tidsstempel: LocalDateTime,
        override val deltagelsePeriode: Periode,
        override val girRett: Boolean,
        // TODO jah: Kunne ønske meg at denne var en enum. Vi sliter vel uansett med å håndtere ukjente verdier?
        override val status: String,
        override val kilde: Tiltakskilde,
    ) : TiltakDeltagelseSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null
    }
}
