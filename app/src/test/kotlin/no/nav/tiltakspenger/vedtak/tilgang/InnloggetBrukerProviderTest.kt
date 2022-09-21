package no.nav.tiltakspenger.vedtak.tilgang

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.ktor.server.auth.jwt.*
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.vedtak.RoleName
import org.junit.jupiter.api.Test
import java.util.*

internal class InnloggetBrukerProviderTest {

    @Test
    fun `sjekk rolle-mapping`() {
        System.setProperty("NAIS_CLUSTER_NAME", "dev-gcp")
        val innloggetBrukerProvider = InnloggetBrukerProvider()

        val principal = mockk<JWTPrincipal>()
        every { principal.getClaim("NAVident", String::class) } returns "H12345"
        every { principal.getClaim("preferred_username", String::class) } returns "test.user@nav.no"
        every { principal.getListClaim("groups", UUID::class) } returns listOf(
            "1b3a2c4d-d620-4fcf-a29b-a6cdadf29680",
            "5ef775f2-61f8-4283-bf3d-8d03f428aa14"
        ).map { UUID.fromString(it) }

        val innloggetBruker = innloggetBrukerProvider.hentInnloggetBruker(principal)
        innloggetBruker.epost shouldBe "test.user@nav.no"
        innloggetBruker.brukernavn shouldBe "test user"
        innloggetBruker.navIdent shouldBe "H12345"
        innloggetBruker.roller shouldContainExactlyInAnyOrder listOf(
            RoleName.SAKSBEHANDLER,
            RoleName.STRENGT_FORTROLIG_ADRESSE
        )
    }
}
