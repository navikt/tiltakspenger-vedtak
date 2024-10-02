package no.nav.tiltakspenger.felles

import java.time.LocalDate

fun LocalDate.erHelg(): Boolean {
    return this.dayOfWeek.value in listOf(6, 7)
}
