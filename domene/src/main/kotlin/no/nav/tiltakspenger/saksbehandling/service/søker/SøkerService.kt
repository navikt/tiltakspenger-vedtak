package no.nav.tiltakspenger.saksbehandling.service.søker

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøkerId

interface SøkerService {
    fun hentSøkerIdOrNull(ident: String, saksbehandler: Saksbehandler): SøkerIdDTO?
    fun hentIdentOrNull(søkerId: SøkerId, saksbehandler: Saksbehandler): String?
    fun hentSøkerId(ident: String, saksbehandler: Saksbehandler): SøkerIdDTO
    fun hentIdent(søkerId: SøkerId, saksbehandler: Saksbehandler): String
}

data class SøkerIdDTO(
    val id: String,
)
