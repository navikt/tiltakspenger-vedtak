package no.nav.tiltakspenger.fakes.clients

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider

class JwkFakeProvider(
    private val jwk: Jwk,
) : JwkProvider {
    override fun get(keyId: String): Jwk? {
        return if (jwk.id == keyId) jwk else null
    }
}
