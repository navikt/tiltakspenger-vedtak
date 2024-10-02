package no.nav.tiltakspenger.saksbehandling.ports

interface PoaoTilgangGateway {
    suspend fun evaluerTilgangTilSkjermet(navAnsattIdent: String): Boolean
    suspend fun evaluerTilgangTilFortrolig(navAnsattIdent: String): Boolean
    suspend fun evaluerTilgangTilStrengtFortrolig(navAnsattIdent: String): Boolean
    suspend fun erSkjermet(fnr: String): Boolean
    suspend fun erSkjermetBolk(fnrListe: List<String>): Map<String, Boolean>
}
