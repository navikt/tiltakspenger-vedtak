package no.nav.tiltakspenger.vedtak.tilgang

import io.ktor.server.auth.jwt.*
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.Role
import no.nav.tiltakspenger.vedtak.RoleName
import java.util.*

private val LOG = KotlinLogging.logger {}

class InnloggetBrukerProvider(private val allAvailableRoles: List<Role> = Configuration.allRoles()) {

    private fun epostToBrukernavn(epost: String): String =
        epost.split("@").first().replace(".", " ")

    private fun finnRolleMedUUID(uuidFraClaim: UUID) =
        allAvailableRoles.single { configRole -> configRole.objectId == uuidFraClaim }

    private fun List<UUID>.mapFromUUIDToRoleName(): List<RoleName> =
        this.map { LOG.info { "Mapper rolle $it" }; it }
            .map { finnRolleMedUUID(it).name }

    fun hentInnloggetBruker(principal: JWTPrincipal): InnloggetBruker {
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
