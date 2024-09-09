package no.nav.tiltakspenger.vedtak.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.SessionCounter
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo
import no.nav.tiltakspenger.vedtak.log
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.benk.SaksoversiktPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.statistikk.sak.StatistikkSakRepoImpl
import no.nav.tiltakspenger.vedtak.repository.statistikk.stønad.StatistikkStønadPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.søknad.PostgresSøknadRepo
import no.nav.tiltakspenger.vedtak.repository.utbetaling.UtbetalingsvedtakPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.vedtak.RammevedtakPostgresRepo
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

internal class TestDataHelper(
    dataSource: DataSource,
) {
    private val sessionCounter = SessionCounter(log)
    val sessionFactory = PostgresSessionFactory(dataSource, sessionCounter)
    val personopplysningerRepo = PersonopplysningerPostgresRepo(sessionFactory)
    val søknadRepo = PostgresSøknadRepo(sessionFactory)
    val behandlingRepo = BehandlingPostgresRepo(sessionFactory)
    val vedtakRepo = RammevedtakPostgresRepo(sessionFactory)
    val sakRepo = SakPostgresRepo(sessionFactory)
    val saksoversiktRepo = SaksoversiktPostgresRepo(sessionFactory)
    val statistikkSakRepo = StatistikkSakRepoImpl(sessionFactory)
    val statistikkStønadRepo = StatistikkStønadPostgresRepo(sessionFactory)
    val meldekortRepo = MeldekortPostgresRepo(sessionFactory)
    val utbetalingsvedtakRepo: UtbetalingsvedtakRepo = UtbetalingsvedtakPostgresRepo(sessionFactory)
}

private fun migrateDatabase(dataSource: DataSource) =
    Flyway
        .configure()
        .loggers("slf4j")
        .encoding("UTF-8")
        .locations("db/migration")
        .dataSource(dataSource)
        .cleanDisabled(false)
        .cleanOnValidationError(true)
        .load()
        .migrate()

fun withMigratedDb(test: (dataSource: DataSource) -> Unit) {
    val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
    postgres.start()

    val dataSource =
        HikariDataSource(
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
