package no.nav.tiltakspenger.saksbehandling.domene.stønadsdager

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface StønadsdagerSaksopplysning {
    val tiltakNavn: String
    val antallDager: Int
    val periode: Periode
    val kilde: Tiltakskilde
    val tidsstempel: LocalDateTime
    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: Saksbehandler?

    data class Register(
        override val tiltakNavn: String,
        override val antallDager: Int,
        override val periode: Periode,
        override val tidsstempel: LocalDateTime,
        override val kilde: Tiltakskilde,
    ) : StønadsdagerSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null
    }
}
