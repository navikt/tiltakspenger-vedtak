package no.nav.tiltakspenger.saksbehandling.domene.stønadsdager

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import java.time.LocalDateTime

sealed interface StønadsdagerSaksopplysning {
    val tiltakNavn: String
    val eksternDeltagelseId: String
    val gjennomføringId: String?
    val antallDager: Int
    val periode: Periode
    val kilde: Tiltakskilde
    val tidsstempel: LocalDateTime

    data class Register(
        override val tiltakNavn: String,
        override val eksternDeltagelseId: String,
        override val gjennomføringId: String?,
        override val antallDager: Int,
        override val periode: Periode,
        override val tidsstempel: LocalDateTime,
        override val kilde: Tiltakskilde,
    ) : StønadsdagerSaksopplysning {
        fun krymp(nyPeriode: Periode): Register {
            if (periode == nyPeriode) return this
            require(periode.inneholderHele(nyPeriode)) { "Ny periode ($nyPeriode) må være innenfor saksopplysningen periode ($periode)" }
            return this.copy(periode = nyPeriode)
        }
    }
}
