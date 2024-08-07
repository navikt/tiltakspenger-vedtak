package no.nav.tiltakspenger.vedtak.db

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

object DataSourceSetup {
    private const val MAX_POOLS = 5
    const val DB_USERNAME_KEY = "DB_USERNAME"
    const val DB_PASSWORD_KEY = "DB_PASSWORD"
    const val DB_DATABASE_KEY = "DB_DATABASE"
    const val DB_HOST_KEY = "DB_HOST"
    const val DB_PORT_KEY = "DB_PORT"

    private fun getEnvOrProp(key: String) = System.getenv(key) ?: System.getProperty(key)

    fun createDatasource(): HikariDataSource {
        LOG.info {
            "Kobler til Postgres '${getEnvOrProp(DB_USERNAME_KEY)}:xxx@" +
                "${getEnvOrProp(DB_HOST_KEY)}:${getEnvOrProp(DB_PORT_KEY)}/${getEnvOrProp(DB_DATABASE_KEY)}'"
        }

        return HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", getEnvOrProp(DB_HOST_KEY))
            addDataSourceProperty("portNumber", getEnvOrProp(DB_PORT_KEY))
            addDataSourceProperty("databaseName", getEnvOrProp(DB_DATABASE_KEY))
            addDataSourceProperty("user", getEnvOrProp(DB_USERNAME_KEY))
            addDataSourceProperty("password", getEnvOrProp(DB_PASSWORD_KEY))
            initializationFailTimeout = 5000
            maximumPoolSize = MAX_POOLS
        }
    }
}
