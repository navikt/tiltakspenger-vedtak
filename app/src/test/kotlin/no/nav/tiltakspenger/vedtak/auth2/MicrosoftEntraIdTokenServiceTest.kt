package no.nav.tiltakspenger.vedtak.auth2

import arrow.core.left
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.marcinziolo.kotlin.wiremock.equalTo
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.returns
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.common.JwtGenerator
import no.nav.tiltakspenger.common.withWireMockServer
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.Roller
import no.nav.tiltakspenger.vedtak.AdRolle
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.KunneIkkeHenteJwk
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.UgyldigToken.KunneIkkeDekodeToken
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.UgyldigToken.KunneIkkeVerifisereToken
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.UgyldigToken.UlikKid
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

internal class MicrosoftEntraIdTokenServiceTest {

    @Test
    fun `mapper autoriserte roller riktig`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val saksbehandlerUUID = UUID.randomUUID()
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            groups = listOf(saksbehandlerUUID.toString()),
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = listOf(AdRolle(Rolle.SAKSBEHANDLER, saksbehandlerUUID.toString())),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt).getOrNull()!! shouldBe Saksbehandler(
                    navIdent = "Z12345",
                    brukernavn = "Sak Behandler",
                    epost = "Sak.Behandler@nav.no",
                    roller = Roller(listOf(Rolle.SAKSBEHANDLER)),
                )
            }
        }
    }

    @Test
    fun `ukjent rolle skal filtreres bort`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val ukjentRolle = UUID.randomUUID()
        val jwtGenerator = JwtGenerator()
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            groups = listOf(ukjentRolle.toString()),
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = listOf(AdRolle(Rolle.SAKSBEHANDLER, UUID.randomUUID().toString())),
            )
            wiremock.prepareJwkResponse(jwtGenerator.jwkAsString)

            runTest {
                tokenService.validerOgHentBruker(jwt).getOrNull()!! shouldBe Saksbehandler(
                    navIdent = "Z12345",
                    brukernavn = "Sak Behandler",
                    epost = "Sak.Behandler@nav.no",
                    roller = Roller(emptyList()),
                )
            }
        }
    }

    @Test
    fun `andre kall cacher`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt).getOrNull()!!
                wiremock.verify(1, getRequestedFor(urlEqualTo("/")))
                tokenService.validerOgHentBruker(jwt).getOrNull()!!
                wiremock.verify(1, getRequestedFor(urlEqualTo("/")))
            }
        }
    }

    @Test
    fun `andre kall cacher ikke ved ukjent kid`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator1 = JwtGenerator()
        val jwk1 = jwtGenerator1.jwkAsString
        val jwt1 = jwtGenerator1.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
        )
        val jwtGenerator2 = JwtGenerator()
        val jwt2 = jwtGenerator2.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk1)

            runTest {
                tokenService.validerOgHentBruker(jwt1).getOrNull()!!
                wiremock.verify(1, getRequestedFor(urlEqualTo("/")))
                tokenService.validerOgHentBruker(jwt2) shouldBe UlikKid.left()
                wiremock.verify(2, getRequestedFor(urlEqualTo("/")))
            }
        }
    }

    @Test
    fun `krever at jwt er signert av riktig jwk`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val keyId = UUID.randomUUID().toString()
        val jwtGenerator1 = JwtGenerator(jwkKeyId = keyId)
        val jwk1 = jwtGenerator1.jwkAsString
        val jwtGenerator2 = JwtGenerator()
        val jwt2 = jwtGenerator2.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            jwtKeyId = keyId,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk1)

            runTest {
                tokenService.validerOgHentBruker(jwt2) shouldBe KunneIkkeVerifisereToken.left()
            }
        }
    }

    @Test
    fun `ugyldig JWT`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val invalidJwt = "invalid.jwt.token"

        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            runTest {
                tokenService.validerOgHentBruker(invalidJwt) shouldBe KunneIkkeDekodeToken.left()
            }
        }
    }

    @Test
    fun `token exp margin på 1 sekund feiler`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            expiresAt = Instant.now().plusMillis(1000),
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe KunneIkkeVerifisereToken.left()
            }
        }
    }

    @Test
    fun `token exp margin på 5 sekunder er OK`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            expiresAt = Instant.now().plusSeconds(5),
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt).shouldBeRight()
            }
        }
    }

    @Test
    fun `issued at tåler 5 sekunder leeway`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            issuedAt = Instant.now().plusSeconds(5),
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt).shouldBeRight()
            }
        }
    }

    @Test
    fun `issued at er for langt fram i tid`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            issuedAt = Instant.now().plusSeconds(10),
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe KunneIkkeVerifisereToken.left()
            }
        }
    }

    @Test
    fun `not before tåler 5 sekunder leeway`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            notBefore = Instant.now().plusSeconds(5),
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt).shouldBeRight()
            }
        }
    }

    @Test
    fun `not before er for langt fram i tid`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            notBefore = Instant.now().plusSeconds(10),
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe KunneIkkeVerifisereToken.left()
            }
        }
    }

    @Test
    fun `uforventet issuer`() {
        val issuer = "test-issuer"
        val wrongIssuer = "wrong-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
        )

        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = wrongIssuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe KunneIkkeVerifisereToken.left()
            }
        }
    }

    @Test
    fun `uforventet audience`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val ukjentAudience = "ukjent-audience"
        val jwtGenerator = JwtGenerator()
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = ukjentAudience,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe KunneIkkeVerifisereToken.left()
            }
        }
    }

    @Test
    fun `mangler jwk`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator()
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse("""{"keys":[]}""")

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe KunneIkkeHenteJwk.left()
            }
        }
    }

    @Test
    fun `jwk service unavailable`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator(jwkKeyId = "jwk-key-id")
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            jwtKeyId = "jwt-key-id",
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.get {
                url equalTo "/"
            } returns {
                statusCode = 503
                body = "Service Unavailable"
            }

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe KunneIkkeHenteJwk.left()
            }
        }
    }

    @Test
    fun `ulik kid`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator(jwkKeyId = "jwk-key-id")
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            jwtKeyId = "jwt-key-id",
        )

        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe UlikKid.left()
            }
        }
    }

    @Test
    fun `mangler NAVident claim`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator(jwkKeyId = "jwk-key-id")
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            navIdent = null,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe Valideringsfeil.UgyldigToken.ManglerClaim("NAVident")
                    .left()
            }
        }
    }

    @Test
    fun `mangler preferred_username claim`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator(jwkKeyId = "jwk-key-id")
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            preferredUsername = null,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe Valideringsfeil.UgyldigToken.ManglerClaim("preferred_username")
                    .left()
            }
        }
    }

    @Test
    fun `mangler groups claim`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator(jwkKeyId = "jwk-key-id")
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSaksbehandler(
            issuer = issuer,
            audience = clientId,
            groups = null,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe Valideringsfeil.UgyldigToken.ManglerClaim("groups")
                    .left()
            }
        }
    }

    @Test
    fun `systembruker mangler roles claim`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator(jwkKeyId = "jwk-key-id")
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSystembruker(
            issuer = issuer,
            audience = clientId,
            roles = null,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe Valideringsfeil.UgyldigToken.ManglerClaim("roles").left()
            }
        }
    }

    @Test
    fun `systembruker mangler azp_name claim`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator(jwkKeyId = "jwk-key-id")
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSystembruker(
            issuer = issuer,
            audience = clientId,
            azpName = null,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe Valideringsfeil.UgyldigToken.ManglerClaim("azp_name")
                    .left()
            }
        }
    }

    @Test
    fun `systembruker mangler oid claim`() {
        val issuer = "test-issuer"
        val clientId = "test-client-id"
        val jwtGenerator = JwtGenerator(jwkKeyId = "jwk-key-id")
        val jwk = jwtGenerator.jwkAsString
        val jwt = jwtGenerator.createJwtForSystembruker(
            issuer = issuer,
            audience = clientId,
            oid = null,
        )
        withWireMockServer { wiremock ->
            val tokenService = MicrosoftEntraIdTokenService(
                url = wiremock.baseUrl(),
                issuer = issuer,
                clientId = clientId,
                autoriserteBrukerroller = emptyList(),
            )
            wiremock.prepareJwkResponse(jwk)

            runTest {
                tokenService.validerOgHentBruker(jwt) shouldBe Valideringsfeil.UgyldigToken.ManglerClaim("oid").left()
            }
        }
    }
}

private fun WireMockServer.prepareJwkResponse(jwk: String) {
    this.get {
        url equalTo "/"
    } returns {
        statusCode = 200
        header = "Content-Type" to "application/json"
        body = jwk
    }
}
