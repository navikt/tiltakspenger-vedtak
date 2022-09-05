package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate

data class PersonopplysningerDTO(
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val adressebeskyttelseGradering: AdressebeskyttelseGradering?,
    val gtKommune: String?,
    val gtBydel: String?,
    val gtLand: String?,
    val barn: List<BarnDTO>,
)
