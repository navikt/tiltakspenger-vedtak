package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate

const val ALDER_BARNETILLEGG: Long = 16

data class BarnDTO(
    val ident: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fødselsdato: LocalDate,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering,
) {
    fun kanGiRettPåBarnetillegg() = fødselsdato.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusDays(1))
}

data class BarnUtenFolkeregisteridentifikatorDTO(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val fødselsdato: LocalDate?,
) {
    fun kanGiRettPåBarnetillegg() =
        fødselsdato?.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusDays(1)) ?: true
}
