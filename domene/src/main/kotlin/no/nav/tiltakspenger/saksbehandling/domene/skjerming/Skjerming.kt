package no.nav.tiltakspenger.saksbehandling.domene.skjerming

import no.nav.tiltakspenger.felles.Tidsstempler
import no.nav.tiltakspenger.libs.common.Fnr
import java.time.LocalDateTime

data class Skjerming(
    val søker: SkjermingPerson,
    val barn: List<SkjermingPerson>,
    val innhentet: LocalDateTime,
) : Tidsstempler {
    override fun tidsstempelKilde() = innhentet

    override fun tidsstempelHosOss() = innhentet

    data class SkjermingPerson(
        val fnr: Fnr,
        val skjerming: Boolean,
    )
}
