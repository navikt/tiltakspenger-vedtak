package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate

data class BarnDTO(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val fødselsdato: LocalDate?
)