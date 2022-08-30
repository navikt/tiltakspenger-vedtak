package no.nav.tiltakspenger.vedtak.db

import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class DataSourceTest {
    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgreSQLContainer = TestPostgreSQLContainer.instance
    }

    @Test
    fun `flyway migrations skal kjøre uten feil`() {
        flywayMigrate()
    }
}
