package no.nav.tiltakspenger.vedtak.innsending

import java.time.LocalDateTime

interface Tidsstempler {
    fun tidsstempelKilde(): LocalDateTime
    fun tidsstempelHosOss(): LocalDateTime
}
