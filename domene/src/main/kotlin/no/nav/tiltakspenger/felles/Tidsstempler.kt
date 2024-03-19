package no.nav.tiltakspenger.felles

import java.time.LocalDateTime

interface Tidsstempler {
    fun tidsstempelKilde(): LocalDateTime
    fun tidsstempelHosOss(): LocalDateTime
}
