package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

interface Tidsstempler {
    fun oppdatert(): LocalDateTime
    fun innhentet(): LocalDateTime
}