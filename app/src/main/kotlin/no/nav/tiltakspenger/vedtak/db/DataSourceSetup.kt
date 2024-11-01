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
            "Kobler til Postgres '${config.brukernavn}:xxx@" +
                "${config.host}:${config.port}/${config.database}'"
        }

        return HikariDataSource().apply {
            jdbcUrl = config.url
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", config.host)
            addDataSourceProperty("portNumber", config.port)
            addDataSourceProperty("databaseName", config.database)
            addDataSourceProperty("user", config.brukernavn)
            addDataSourceProperty("password", config.passord)
            initializationFailTimeout = 5000
            maximumPoolSize = MAX_POOLS
        }.also {
            flywayMigrate(it)
        }
    }
}
