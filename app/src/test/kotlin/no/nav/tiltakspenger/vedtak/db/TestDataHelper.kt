package no.nav.tiltakspenger.vedtak.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.SessionCounter
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo
import no.nav.tiltakspenger.vedtak.log
import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.benk.SaksoversiktPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortRepoImpl
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerBarnMedIdentRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerBarnUtenIdentRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresPersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import no.nav.tiltakspenger.vedtak.repository.statistikk.sak.StatistikkSakRepoImpl
import no.nav.tiltakspenger.vedtak.repository.statistikk.stønad.StatistikkStønadRepoImpl
import no.nav.tiltakspenger.vedtak.repository.søknad.BarnetilleggDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.PostgresSøknadRepo
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadTiltakDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.VedleggDAO
import no.nav.tiltakspenger.vedtak.repository.utbetaling.UtbetalingsvedtakRepoImpl
import no.nav.tiltakspenger.vedtak.repository.vedtak.RammevedtakRepoImpl
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

internal class TestDataHelper(
    dataSource: DataSource,
) {
    val sessionCounter = SessionCounter(log)
    val sessionFactory = PostgresSessionFactory(dataSource, sessionCounter)

    val personopplysningerBarnUtenIdentRepo = PersonopplysningerBarnUtenIdentRepo()
    val personopplysningerBarnMedIdentRepo = PersonopplysningerBarnMedIdentRepo()
    val personopplysningerRepo =
        PostgresPersonopplysningerRepo(
            sessionFactory = sessionFactory,
            barnMedIdentDAO = personopplysningerBarnMedIdentRepo,
            barnUtenIdentDAO = personopplysningerBarnUtenIdentRepo,
        )

    val barnetilleggDAO = BarnetilleggDAO()
    val søknadTiltakDAO = SøknadTiltakDAO()
    val vedleggDAO = VedleggDAO()
    val søknadDAO =
        SøknadDAO(
            barnetilleggDAO = barnetilleggDAO,
            tiltakDAO = søknadTiltakDAO,
            vedleggDAO = vedleggDAO,
        )
    val søknadRepo =
        PostgresSøknadRepo(
            sessionFactory = sessionFactory,
            søknadDAO = søknadDAO,
        )

    val behandlingRepo =
        PostgresBehandlingRepo(
            sessionFactory = sessionFactory,
            søknadDAO = søknadDAO,
        )

    val vedtakRepo =
        RammevedtakRepoImpl(
            behandlingRepo = behandlingRepo,
            sessionFactory = sessionFactory,
        )
    val sakRepo =
        PostgresSakRepo(
            behandlingRepo = behandlingRepo,
            personopplysningerRepo = personopplysningerRepo,
            vedtakDAO = vedtakRepo,
            sessionFactory = sessionFactory,
        )
    val saksoversiktRepo =
        SaksoversiktPostgresRepo(
            sessionFactory = sessionFactory,
        )
    val statistikkSakRepo =
        StatistikkSakRepoImpl(
            sessionFactory = sessionFactory,
        )
    val statistikkStønadRepo =
        StatistikkStønadRepoImpl(
            sessionFactory = sessionFactory,
        )
    val meldekortRepo = MeldekortRepoImpl(sessionFactory)

    val utbetalingsvedtakRepo: UtbetalingsvedtakRepo = UtbetalingsvedtakRepoImpl(sessionFactory, meldekortRepo)
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
