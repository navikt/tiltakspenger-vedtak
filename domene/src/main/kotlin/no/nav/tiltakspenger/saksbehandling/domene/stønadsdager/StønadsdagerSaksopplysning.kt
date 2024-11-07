package no.nav.tiltakspenger.saksbehandling.domene.stønadsdager

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import java.time.LocalDateTime

sealed interface StønadsdagerSaksopplysning {
    val tiltakNavn: String
    val eksternId: String
    val antallDager: Int
    val periode: Periode
    val kilde: Tiltakskilde
    val tidsstempel: LocalDateTime

    data class Register(
        override val tiltakNavn: String,
        override val eksternId: String,
        override val antallDager: Int,
        override val periode: Periode,
        override val tidsstempel: LocalDateTime,
        override val kilde: Tiltakskilde,
    ) : StønadsdagerSaksopplysning
}
