package no.nav.tiltakspenger.vedtak.service

import no.nav.tiltakspenger.vedtak.routes.person.PersonDTO
import java.time.LocalDate

interface PersonService {
    fun hentPerson(ident: String): PersonDTO?
    fun hentSøkerOgSøknader(ident: String): SøkerDTO?
}

data class SøkerDTO(
    val ident: String,
    val søknader: List<SøknadDTO>
)

data class SøknadDTO(
    val søknadId: String,
    val arrangoernavn: String,
    val tiltakskode: String,
    val startdato: LocalDate,
    val sluttdato: LocalDate?
)
