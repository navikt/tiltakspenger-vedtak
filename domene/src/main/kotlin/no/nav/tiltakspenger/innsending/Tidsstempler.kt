package no.nav.tiltakspenger.innsending

import java.time.LocalDateTime

interface Tidsstempler {
    fun tidsstempelKilde(): LocalDateTime
    fun tidsstempelHosOss(): LocalDateTime
}
