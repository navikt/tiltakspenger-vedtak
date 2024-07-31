package no.nav.tiltakspenger.saksbehandling.service.søker

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.libs.common.Fnr

interface SøkerService {
    fun hentSøkerIdOrNull(fnr: Fnr, saksbehandler: Saksbehandler): SøkerIdDTO?
    fun hentIdentOrNull(søkerId: SøkerId, saksbehandler: Saksbehandler): Fnr?
    fun hentSøkerId(fnr: Fnr, saksbehandler: Saksbehandler): SøkerIdDTO
    fun hentIdent(søkerId: SøkerId, saksbehandler: Saksbehandler): Fnr
}

data class SøkerIdDTO(
    val id: String,
)
