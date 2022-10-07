package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate

data class BarnDTO(
    val ident: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fødselsdato: LocalDate,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering,
)

data class BarnUtenFolkeregisteridentifikatorDTO(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val fødselsdato: LocalDate?,
)
