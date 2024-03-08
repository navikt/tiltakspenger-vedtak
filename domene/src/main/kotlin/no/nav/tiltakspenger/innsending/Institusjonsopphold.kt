package no.nav.tiltakspenger.innsending

import java.time.LocalDate
import java.time.LocalDateTime

data class Institusjonsopphold(
    val oppholdId: Long,
    val tssEksternId: String,
    val organisasjonsnummer: String?,
    val institusjonstype: String?,
    val kategori: String?,
    val startdato: LocalDate,
    val faktiskSluttdato: LocalDate?,
    val kilde: String,
    val endretAv: String?,
    val endringstidspunkt: LocalDateTime?,
    val institusjonsnavn: String?,
    val avdelingsnavn: String?,
)
