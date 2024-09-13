package no.nav.tiltakspenger.vedtak.db

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Configuration.database

private val LOG = KotlinLogging.logger {}

object DataSourceSetup {
    private const val MAX_POOLS = 5
    private val config = database()

    private fun getEnvOrProp(key: String) = System.getenv(key) ?: System.getProperty(key)

    fun createDatasource(): HikariDataSource {
        LOG.info {
            "Kobler til Postgres '${getEnvOrProp(config.brukernavn)}:xxx@" +
                "${getEnvOrProp(config.host)}:${getEnvOrProp(config.port.toString())}/${getEnvOrProp(config.database)}'"
        }

        return HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", config.host)
            addDataSourceProperty("portNumber", config.port)
            addDataSourceProperty("databaseName", config.database)
            addDataSourceProperty("user", config.brukernavn)
            addDataSourceProperty("password", config.passord)
            initializationFailTimeout = 5000
            maximumPoolSize = MAX_POOLS
        }
    }
}
