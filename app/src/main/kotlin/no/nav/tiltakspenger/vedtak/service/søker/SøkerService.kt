package no.nav.tiltakspenger.vedtak.service.søker

import java.time.LocalDate

interface SøkerService {
    fun hentSøkerOgSøknader(ident: String): SøkerDTO?
}

data class SøkerDTO(
    val ident: String,
    val søknader: List<ListeSøknadDTO>
)

data class ListeSøknadDTO(
    val søknadId: String,
    val arrangoernavn: String?,
    val tiltakskode: String?,
    val startdato: LocalDate,
    val sluttdato: LocalDate?
)
