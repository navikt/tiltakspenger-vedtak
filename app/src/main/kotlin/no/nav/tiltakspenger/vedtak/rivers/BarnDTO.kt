package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate

const val ALDER_BARNETILLEGG = 16L
const val SIKKERHETSMARGIN_ÅR = 2L // søknaden sender med barn opp til 18 år. Vi lagrer det samme just in case

data class BarnDTO(
    val ident: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fødselsdato: LocalDate,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering,
) {
    fun kanGiRettPåBarnetillegg() =
        fødselsdato.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR))
}

data class BarnUtenFolkeregisteridentifikatorDTO(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val fødselsdato: LocalDate?,
) {
    fun kanGiRettPåBarnetillegg() =
        fødselsdato?.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR)) ?: true
}
