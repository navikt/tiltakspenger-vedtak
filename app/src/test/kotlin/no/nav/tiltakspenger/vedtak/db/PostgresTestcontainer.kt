package no.nav.tiltakspenger.vedtak.db

import no.nav.tiltakspenger.vedtak.db.DataSource.DB_DATABASE_KEY
import no.nav.tiltakspenger.vedtak.db.DataSource.DB_HOST_KEY
import no.nav.tiltakspenger.vedtak.db.DataSource.DB_PASSWORD_KEY
import no.nav.tiltakspenger.vedtak.db.DataSource.DB_PORT_KEY
import no.nav.tiltakspenger.vedtak.db.DataSource.DB_USERNAME_KEY
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy

object PostgresTestcontainer : PostgreSQLContainer<PostgresTestcontainer>("postgres:14.4") {

    val container = waitingFor(HostPortWaitStrategy())!!

    override fun start() {
        super.start()
        System.setProperty(DB_HOST_KEY, container.host)
        System.setProperty(DB_PORT_KEY, container.getMappedPort(POSTGRESQL_PORT).toString())
        System.setProperty(DB_DATABASE_KEY, container.databaseName)
        System.setProperty(DB_USERNAME_KEY, container.username)
        System.setProperty(DB_PASSWORD_KEY, container.password)
    }

    override fun stop() {
        System.clearProperty(DB_HOST_KEY)
        System.clearProperty(DB_PORT_KEY)
        System.clearProperty(DB_DATABASE_KEY)
        System.clearProperty(DB_USERNAME_KEY)
        System.clearProperty(DB_PASSWORD_KEY)
    }
}
