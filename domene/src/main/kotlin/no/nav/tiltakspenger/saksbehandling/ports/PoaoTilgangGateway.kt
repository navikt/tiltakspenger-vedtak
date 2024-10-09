package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.Fnr

interface PoaoTilgangGateway {
    suspend fun evaluerTilgangTilSkjermet(navAnsattIdent: String): Boolean
    suspend fun evaluerTilgangTilFortrolig(navAnsattIdent: String): Boolean
    suspend fun evaluerTilgangTilStrengtFortrolig(navAnsattIdent: String): Boolean
    suspend fun erSkjermet(fnr: Fnr): Boolean
    suspend fun erSkjermetBolk(fnrListe: List<Fnr>): Map<Fnr, Boolean>
}
