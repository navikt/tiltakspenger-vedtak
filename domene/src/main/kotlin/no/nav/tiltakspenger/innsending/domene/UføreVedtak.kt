package no.nav.tiltakspenger.innsending.domene

import no.nav.tiltakspenger.felles.Tidsstempler
import no.nav.tiltakspenger.felles.UføreVedtakId
import java.time.LocalDate
import java.time.LocalDateTime

data class UføreVedtak(
    val id: UføreVedtakId,
    val harUføregrad: Boolean,
    val datoUfør: LocalDate?,
    val virkDato: LocalDate?,
    val innhentet: LocalDateTime,
) : Tidsstempler {

    override fun tidsstempelKilde() = innhentet

    override fun tidsstempelHosOss() = innhentet
}
