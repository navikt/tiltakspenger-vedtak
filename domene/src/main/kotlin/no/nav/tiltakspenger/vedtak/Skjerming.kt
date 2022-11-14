package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

class Skjerming(val ident: String, val skjerming: Boolean, val innhentet: LocalDateTime) : Tidsstempler {

    override fun tidsstempelKilde() = innhentet

    override fun tidsstempelHosOss() = innhentet
}
