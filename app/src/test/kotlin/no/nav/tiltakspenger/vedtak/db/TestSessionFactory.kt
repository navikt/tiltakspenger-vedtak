package no.nav.tiltakspenger.vedtak.db

import mu.KotlinLogging
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.SessionCounter

fun testSessionFactory(): PostgresSessionFactory {
    val logger = KotlinLogging.logger("logger")
    val sessionCounter = SessionCounter(logger)
    return PostgresSessionFactory(DataSource.hikariDataSource, sessionCounter)
}
