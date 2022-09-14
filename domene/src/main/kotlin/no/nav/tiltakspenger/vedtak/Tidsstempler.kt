package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

interface Tidsstempler {
    fun tidsstempelKilde(): LocalDateTime
    fun tidsstempelHosOss(): LocalDateTime
}
