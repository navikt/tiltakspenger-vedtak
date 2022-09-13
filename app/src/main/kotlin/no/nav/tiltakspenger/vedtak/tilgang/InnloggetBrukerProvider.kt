package no.nav.tiltakspenger.vedtak.tilgang

import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.*
import no.nav.tiltakspenger.vedtak.Role
import no.nav.tiltakspenger.vedtak.RoleName

class InnloggetBrukerProvider(private val allAvailableRoles: List<Role>) {

    private fun epostToBrukernavn(epost: String): String =
        epost.split("@").first().replace(".", " ")

    private fun List<UUID>.mapFromUUIDToRoleName(): List<RoleName> =
        this.map { claimObjectId -> allAvailableRoles.single { configRole -> configRole.objectId == claimObjectId }.name }

    suspend fun hentInnloggetBruker(principal: JWTPrincipal): InnloggetBruker {
        val ident = requireNotNull(principal.getClaim("NAVident", String::class)) { "NAVident er null i token" }
        val epost = requireNotNull(
            principal.getClaim("preferred_username", String::class)
        ) { "preferred_username er null i token" }
        val roller = principal.getListClaim("groups", UUID::class).mapFromUUIDToRoleName()

        return InnloggetBruker(
            navIdent = ident,
            brukernavn = epostToBrukernavn(epost),
            epost = epost,
            roller = roller,
        )
    }
}


data class InnloggetBruker(val navIdent: String, val brukernavn: String, val epost: String, val roller: List<RoleName>)
