package no.nav.tiltakspenger.vedtak.db

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Configuration.database

private val LOG = KotlinLogging.logger {}

object DataSourceSetup {
    private const val MAX_POOLS = 5
    private val config = database()

    fun createDatasource(): HikariDataSource {
        LOG.info {
            "Kobler til Postgres. Bruker bare jdbc-urlen i config (+ timeout og maxpools)."
        }

        return HikariDataSource().apply {
            jdbcUrl = config.url
            initializationFailTimeout = 5000
            maximumPoolSize = MAX_POOLS
        }.also {
            flywayMigrate(it)
        }
    }
}
