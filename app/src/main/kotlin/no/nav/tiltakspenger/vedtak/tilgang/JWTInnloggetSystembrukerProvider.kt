package no.nav.tiltakspenger.vedtak.tilgang

import io.ktor.server.auth.jwt.JWTPrincipal
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Systembruker

private val LOG = KotlinLogging.logger {}

class JWTInnloggetSystembrukerProvider(
    private val allAvailableRoles: List<Rolle> = listOf(Rolle.LAGE_HENDELSER)
) : InnloggetSystembrukerProvider {
    override fun hentSystembruker(principal: JWTPrincipal): Systembruker {
        val azpName = requireNotNull(principal.getClaim("azp_name", String::class)) { "azp_name er null i token" }
        val rollerFraToken = principal.getListClaim("roles", String::class).map { it.uppercase() }.also {
            LOG.info { "Vi fant disse rollene i systemtoken $it" }
        }

        val roller = allAvailableRoles.filter { rolle ->
            rollerFraToken.contains(rolle.name)
        }

        return Systembruker(
            brukernavn = azpName,
            roller = roller,
        )
    }
}
