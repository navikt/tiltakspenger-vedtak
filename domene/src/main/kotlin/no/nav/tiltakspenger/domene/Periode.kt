package no.nav.tiltakspenger.domene

import java.time.LocalDate

class Periode(val fra: LocalDate, val til: LocalDate) : ClosedRange<LocalDate> {
    override val endInclusive: LocalDate
        get() = til
    override val start: LocalDate
        get() = fra
}
