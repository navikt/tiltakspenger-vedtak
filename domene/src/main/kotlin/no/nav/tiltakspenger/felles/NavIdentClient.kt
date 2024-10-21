package no.nav.tiltakspenger.felles

interface NavIdentClient {
    suspend fun hentNavnForNavIdent(navIdent: String): String
}
