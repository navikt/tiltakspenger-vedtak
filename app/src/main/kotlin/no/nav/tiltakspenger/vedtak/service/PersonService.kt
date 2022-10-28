package no.nav.tiltakspenger.vedtak.service

import no.nav.tiltakspenger.vedtak.routes.person.BehandlingDTO
import java.time.LocalDate

interface PersonService {
    fun hentSøkerOgSøknader(ident: String): SøkerDTO?
    fun hentSøknad(søknadId: String): BehandlingDTO?
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
