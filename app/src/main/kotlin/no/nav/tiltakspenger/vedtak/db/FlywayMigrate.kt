package no.nav.tiltakspenger.vedtak.db

import org.flywaydb.core.Flyway

fun flywayMigrate() {
    val flyway = Flyway.configure()
        .dataSource(DataSource.hikariDataSource)
        .cleanDisabled(false)
        .cleanOnValidationError(true)
        .load()
    flyway.migrate()
}

fun flywayClean() {
    val flyway = Flyway.configure()
        .dataSource(DataSource.hikariDataSource)
        .load()
    flyway.clean()
    flyway.migrate()
}
