package no.nav.tiltakspenger.vedtak.db

import org.flywaydb.core.Flyway

private fun flyway() = Flyway
    .configure()
    .dataSource(DataSource.hikariDataSource)
    .cleanDisabled(false)
    .cleanOnValidationError(true)
    .load()

fun flywayMigrate() {
    flyway().migrate()
}

fun flywayCleanAndMigrate() {
    val flyway = flyway()
    flyway.clean()
    flyway.migrate()
}
