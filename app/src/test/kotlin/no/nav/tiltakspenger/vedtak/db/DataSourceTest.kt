package no.nav.tiltakspenger.vedtak.db

import org.flywaydb.core.internal.exception.FlywaySqlException
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class DataSourceTest {
    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @Test
    fun `flyway migrations skal kjøre uten feil`() {
        /**
         * The colima docker environment has another behavior when bootstrapping container. It is possible to fail to make a connection right after
         * PostgreSQLContainer.getMappedPort(). Most probably it's an issue of Colima that should be addressed, but right now the workaround suggested: retry after one second.
         * https://github.com/abiosoft/colima/issues/71
         */
        try {
            flywayMigrate()
        } catch (e: FlywaySqlException) {
            e.message?.let {
                if (it.contains("SQL State  : 08001")) {
                    Thread.sleep(1000)
                    flywayMigrate()
                }
            }
        }
    }
}
