package no.nav.tiltakspenger.vedtak.db

import no.nav.tiltakspenger.vedtak.db.DataSource.DB_DATABASE_KEY
import no.nav.tiltakspenger.vedtak.db.DataSource.DB_HOST_KEY
import no.nav.tiltakspenger.vedtak.db.DataSource.DB_PASSWORD_KEY
import no.nav.tiltakspenger.vedtak.db.DataSource.DB_PORT_KEY
import no.nav.tiltakspenger.vedtak.db.DataSource.DB_USERNAME_KEY
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy

class TestPostgreSQLContainer private constructor() : PostgreSQLContainer<TestPostgreSQLContainer?>(IMAGE_VERSION) {

    companion object {
        private const val IMAGE_VERSION = "postgres:14.4"

        private val CONTAINER: TestPostgreSQLContainer = TestPostgreSQLContainer().waitingFor(HostPortWaitStrategy())!!

        val instance: TestPostgreSQLContainer
            get() {
                return CONTAINER
            }
    }

    override fun start() {
        super.start()
        System.setProperty(DB_HOST_KEY, CONTAINER.host)
        System.setProperty(DB_PORT_KEY, CONTAINER.getMappedPort(POSTGRESQL_PORT).toString())
        System.setProperty(DB_DATABASE_KEY, CONTAINER.databaseName)
        System.setProperty(DB_USERNAME_KEY, CONTAINER.username)
        System.setProperty(DB_PASSWORD_KEY, CONTAINER.password)
    }

    override fun stop() {
        System.clearProperty(DB_HOST_KEY)
        System.clearProperty(DB_PORT_KEY)
        System.clearProperty(DB_DATABASE_KEY)
        System.clearProperty(DB_USERNAME_KEY)
        System.clearProperty(DB_PASSWORD_KEY)
    }
}
