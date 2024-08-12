package no.nav.tiltakspenger.vedtak.auth

import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.AccessToken
import java.time.LocalDateTime
import kotlin.math.min

class TokenCache {
    private val log = KotlinLogging.logger {}
    var token: AccessToken? = null
        private set
    private var expires: LocalDateTime? = null

    internal fun isExpired(): Boolean = expires?.isBefore(LocalDateTime.now()) ?: true

    internal fun update(
        accessToken: AccessToken,
        expiresIn: Long,
    ) {
        log.info { "Oppdaterer token cache med token som utløper om $expiresIn sekunder" }
        token = accessToken
        expires = LocalDateTime.now().plusSeconds(expiresIn - min(expiresIn, MARGIN))
    }

    companion object {
        private const val MARGIN = 42L
    }
}
