package no.nav.tiltakspenger.vedtak.innsending

import java.time.LocalDateTime

data class SkjermingPerson(
    val ident: String,
    val skjerming: Boolean,
)

data class Skjerming(
    val søker: SkjermingPerson,
    val barn: List<SkjermingPerson>,
    val innhentet: LocalDateTime,
) : Tidsstempler {

    override fun tidsstempelKilde() = innhentet

    override fun tidsstempelHosOss() = innhentet
}
