package no.nav.tiltakspenger.vedtak.tilgang

import io.ktor.server.auth.jwt.JWTPrincipal
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.AdRolle
import no.nav.tiltakspenger.vedtak.Configuration
import java.util.UUID

private val LOG = KotlinLogging.logger {}

class JWTInnloggetSaksbehandlerProvider(
    private val allAvailableRoles: List<AdRolle> = Configuration.alleAdRoller(),
) : InnloggetSaksbehandlerProvider {

    private fun epostToBrukernavn(epost: String): String =
        epost.split("@").first().replace(".", " ")

    private fun finnRolleMedUUID(uuidFraClaim: UUID) =
        allAvailableRoles.single { configRole -> configRole.objectId == uuidFraClaim }

    private fun List<UUID>.mapFromUUIDToRoleName(): List<Rolle> =
        this.map { LOG.debug { "Mapper rolle $it" }; it }
            .map { finnRolleMedUUID(it).name }

    override fun hentSaksbehandler(principal: JWTPrincipal): Saksbehandler {
        val ident = requireNotNull(principal.getClaim("NAVident", String::class)) { "NAVident er null i token" }
        val epost = requireNotNull(
            principal.getClaim("preferred_username", String::class),
        ) { "preferred_username er null i token" }
        val roller = principal.getListClaim("groups", UUID::class).mapFromUUIDToRoleName()

        return Saksbehandler(
            navIdent = ident,
            brukernavn = epostToBrukernavn(epost),
            epost = epost,
            roller = roller,
        )
    }
}
