package no.nav.tiltakspenger.common

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Base64
import java.util.UUID

object JwtAndJwkGenerator {
    fun createJwkJwtPairForSaksbehandler(
        jwkKeyId: String = UUID.randomUUID().toString(),
        jwtKeyId: String = jwkKeyId,
        issuer: String = "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/v2.0",
        subject: String = "test-subject",
        preferredUsername: String? = "F_Z990297.E_Z990297@trygdeetaten.no",
        azpName: String? = "dev-fss:tpts:tiltakspenger-vedtak",
        azp: String? = "744e4092-4215-4e02-87df-a61aaf1b95b5",
        navIdent: String? = "Z990297",
        name: String = "Test Testesen",
        audience: String = "c7adbfbb-1b1e-41f6-9b7a-af9627c04998",
        groups: List<String>? = listOf("ROLE_SAKSBEHANDLER"),
        expiresAt: Instant = Instant.now().plusSeconds(1800),
        issuedAt: Instant = Instant.now().minusSeconds(5),
        notBefore: Instant = Instant.now().minusSeconds(5),
    ): Pair<String, String> {
        val keyPair: KeyPair = generateRsaKeyPair()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val jwk = createJwk(jwkKeyId, publicKey)
        val jwt = createJwtForSaksbehandler(
            keyId = jwtKeyId,
            issuer = issuer,
            subject = subject,
            expiresAt = expiresAt,
            issuedAt = issuedAt,
            notBefore = notBefore,
            privateKey = privateKey,
            preferredUsername = preferredUsername,
            navIdent = navIdent,
            name = name,
            audience = audience,
            groups = groups,
            azp = azp,
            azpName = azpName,
        )
        return Pair(jwk, jwt)
    }

    fun createJwkJwtPairForSystembruker(
        jwkKeyId: String = UUID.randomUUID().toString(),
        jwtKeyId: String = jwkKeyId,
        issuer: String = "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/v2.0",
        subject: String = "test-subject",
        azpName: String? = "dev-fss:tpts:tiltakspenger-vedtak",
        azp: String? = "744e4092-4215-4e02-87df-a61aaf1b95b5",
        name: String = "Test Testesen",
        audience: String = "c7adbfbb-1b1e-41f6-9b7a-af9627c04998",
        roles: List<String>? = listOf("LAGE_HENDELSER", "HENTE_DATA"),
        oid: String? = subject,
        expiresAt: Instant = Instant.now().plusSeconds(1800),
        issuedAt: Instant = Instant.now().minusSeconds(5),
        notBefore: Instant = Instant.now().minusSeconds(5),
    ): Pair<String, String> {
        val keyPair: KeyPair = generateRsaKeyPair()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val jwk = createJwk(jwkKeyId, publicKey)
        val jwt = createJwtForSystembruker(
            keyId = jwtKeyId,
            issuer = issuer,
            subject = subject,
            expiresAt = expiresAt,
            issuedAt = issuedAt,
            notBefore = notBefore,
            privateKey = privateKey,
            azpName = azpName,
            azp = azp,
            name = name,
            audience = audience,
            roles = roles,
            oid = oid,
        )
        return Pair(jwk, jwt)
    }

    private fun generateRsaKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    private fun createJwk(
        keyId: String,
        publicKey: RSAPublicKey,
    ): String {
        val modulus = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.modulus.toByteArray())
        val exponent = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.publicExponent.toByteArray())

        return """
            {"keys":[
        {
            "alg": "RS256",
            "kty": "RSA",
            "use": "sig",
            "kid": "$keyId",
            "n": "$modulus",
            "e": "$exponent"
        }]}
        """.trimIndent()
    }

    private fun createJwtForSaksbehandler(
        keyId: String,
        issuer: String,
        subject: String,
        expiresAt: Instant,
        issuedAt: Instant,
        notBefore: Instant,
        preferredUsername: String?,
        navIdent: String?,
        name: String?,
        audience: String,
        groups: List<String>?,
        azpName: String?,
        azp: String?,
        privateKey: RSAPrivateKey,
    ): String {
        val algorithm = Algorithm.RSA256(null, privateKey)
        return JWT.create()
            .withKeyId(keyId)
            .withIssuer(issuer)
            .withSubject(subject)
            .withAudience(audience)
            .withExpiresAt(expiresAt)
            .withIssuedAt(issuedAt)
            .withNotBefore(notBefore)
            .withClaim("preferred_username", preferredUsername)
            .withClaim("NAVident", navIdent)
            .withClaim("name", name)
            .withClaim("azp_name", azpName)
            .withClaim("azp", azp)
            .withClaim("groups", groups)
            .withClaim("ver", "2.0")
            .sign(algorithm)
    }

    private fun createJwtForSystembruker(
        keyId: String,
        issuer: String,
        subject: String,
        expiresAt: Instant,
        issuedAt: Instant,
        notBefore: Instant,
        name: String?,
        audience: String,
        roles: List<String>?,
        azpName: String?,
        oid: String?,
        azp: String?,
        privateKey: RSAPrivateKey,
    ): String {
        val algorithm = Algorithm.RSA256(null, privateKey)
        return JWT.create()
            .withKeyId(keyId)
            .withIssuer(issuer)
            .withSubject(subject)
            .withAudience(audience)
            .withExpiresAt(expiresAt)
            .withIssuedAt(issuedAt)
            .withNotBefore(notBefore)
            .withClaim("name", name)
            .withClaim("roles", roles)
            .withClaim("azp_name", azpName)
            .withClaim("azp", azp)
            .withClaim("oid", oid)
            .withClaim("idtyp", "app")
            .withClaim("ver", "2.0")
            .sign(algorithm)
    }
}
