package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

interface Kildedata {
    fun oppdatert(): LocalDateTime
    fun innhentet(): LocalDateTime
}