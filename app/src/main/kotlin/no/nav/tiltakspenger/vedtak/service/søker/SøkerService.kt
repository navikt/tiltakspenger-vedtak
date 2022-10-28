package no.nav.tiltakspenger.vedtak.service.søker

import no.nav.tiltakspenger.felles.Saksbehandler
import java.time.LocalDate

interface SøkerService {
    fun hentSøkerOgSøknader(ident: String, saksbehandler: Saksbehandler): SøkerDTO?
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
