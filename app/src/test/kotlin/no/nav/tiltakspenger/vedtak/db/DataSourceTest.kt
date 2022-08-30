package no.nav.tiltakspenger.vedtak.db

import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class DataSourceTest {
    companion object {
        @Container
        @JvmField
        val postgresContainer = PostgresTestcontainer.container
    }

    @Test
    fun `flyway migrations skal kjøre uten feil`() {
        flywayMigrate()
    }
}
