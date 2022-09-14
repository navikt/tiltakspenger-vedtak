package no.nav.tiltakspenger.vedtak.db

import org.flywaydb.core.Flyway

fun flywayMigrate() {
    Flyway.configure()
        .dataSource(DataSource.hikariDataSource)
        .cleanOnValidationError(true)
        .load()
        .migrate()
}

fun flywayRepair() {
    Flyway.configure()
        .dataSource(DataSource.hikariDataSource)
        .load()
        .repair()
}
