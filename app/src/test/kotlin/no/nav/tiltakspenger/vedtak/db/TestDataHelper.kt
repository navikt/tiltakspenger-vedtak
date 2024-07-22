package no.nav.tiltakspenger.vedtak.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.SessionCounter
import no.nav.tiltakspenger.vedtak.log
import no.nav.tiltakspenger.vedtak.repository.behandling.TiltakDAO
import no.nav.tiltakspenger.vedtak.repository.søker.PersonopplysningerDAO
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepositoryImpl
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

internal class TestDataHelper(
    dataSource: DataSource,
) {
    val sessionCounter = SessionCounter(log)
    val sessionFactory = PostgresSessionFactory(dataSource, sessionCounter)

//    val behandlingRepo = PostgresBehandlingRepo(
//        sessionFactory = sessionFactory
//    )

    val personopplysningerDAO = PersonopplysningerDAO()
    val søkerRepo = SøkerRepositoryImpl(
        sessionFactory = sessionFactory,
        personopplysningerDAO = personopplysningerDAO,
    )
    val tiltakDAO = TiltakDAO()
}

private fun migrateDatabase(dataSource: DataSource) = Flyway
    .configure()
    .loggers("slf4j")
    .encoding("UTF-8")
    .locations("db/migration")
    .dataSource(dataSource)
    .cleanDisabled(false)
    .cleanOnValidationError(true)
    .load()
    .migrate()

fun withMigratedDb(
    test: (dataSource: DataSource) -> Unit,
) {
    val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
    postgres.start()

    val dataSource = HikariDataSource(
        HikariConfig().apply {
            this.jdbcUrl = postgres.jdbcUrl
            this.maximumPoolSize = 3
            this.minimumIdle = 1
            this.idleTimeout = 10001
            this.connectionTimeout = 1000
            this.maxLifetime = 30001
            this.username = postgres.username
            this.password = postgres.password
            initializationFailTimeout = 5000
        },
    )

    migrateDatabase(dataSource)
    test(dataSource)
    postgres.stop()
}
