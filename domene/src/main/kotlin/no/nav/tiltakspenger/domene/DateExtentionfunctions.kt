package no.nav.tiltakspenger.domene

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.temporal.ChronoUnit

infix fun Int.januar(year: Int): LocalDate = LocalDate.of(year, Month.JANUARY, this)
fun Int.februar(year: Int): LocalDate = LocalDate.of(year, Month.FEBRUARY, this)
fun Int.mars(year: Int): LocalDate = LocalDate.of(year, Month.MARCH, this)
fun Int.april(year: Int): LocalDate = LocalDate.of(year, Month.APRIL, this)
fun Int.mai(year: Int): LocalDate = LocalDate.of(year, Month.MAY, this)
fun Int.juni(year: Int): LocalDate = LocalDate.of(year, Month.JUNE, this)
fun Int.juli(year: Int): LocalDate = LocalDate.of(year, Month.JULY, this)
fun Int.august(year: Int): LocalDate = LocalDate.of(year, Month.AUGUST, this)
fun Int.september(year: Int): LocalDate = LocalDate.of(year, Month.SEPTEMBER, this)
fun Int.oktober(year: Int): LocalDate = LocalDate.of(year, Month.OCTOBER, this)
fun Int.november(year: Int): LocalDate = LocalDate.of(year, Month.NOVEMBER, this)
fun Int.desember(year: Int): LocalDate = LocalDate.of(year, Month.DECEMBER, this)

fun Int.januarDateTime(year: Int): LocalDateTime = LocalDateTime.of(
    year,
    Month.JANUARY,
    this,
    0,
    0,
).truncatedTo(ChronoUnit.MILLIS)

fun Int.februarDateTime(year: Int): LocalDateTime = LocalDateTime.of(
    year,
    Month.FEBRUARY,
    this,
    0,
    0,
).truncatedTo(ChronoUnit.MILLIS)

fun Int.marsDateTime(year: Int): LocalDateTime = LocalDateTime.of(
    year,
    Month.MARCH,
    this,
    0,
    0,
).truncatedTo(ChronoUnit.MILLIS)

fun nå() = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
// fun LocalDateTime.Companion.nowTrunc() = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
