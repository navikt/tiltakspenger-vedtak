package no.nav.tiltakspenger.saksbehandling.ports

interface TilgangGateway {
    suspend fun evaluerTilgangTilSkjermet(navAnsattIdent: String): Boolean
    suspend fun evaluerTilgangTilFortrolig(navAnsattIdent: String): Boolean
    suspend fun evaluerTilgangTilStrengtFortrolig(navAnsattIdent: String): Boolean
}
