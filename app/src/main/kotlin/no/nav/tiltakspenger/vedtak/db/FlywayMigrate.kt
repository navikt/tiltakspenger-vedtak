package no.nav.tiltakspenger.vedtak.db

import org.flywaydb.core.Flyway

fun flywayMigrate() {
    val flyway = Flyway.configure()
        .dataSource(DataSource.hikariDataSource)
        .cleanOnValidationError(true)
        .load()
    flyway.migrate()
}

fun flywayRepair() {
    Flyway.configure()
        .dataSource(DataSource.hikariDataSource)
        .load()
        .repair()
}
