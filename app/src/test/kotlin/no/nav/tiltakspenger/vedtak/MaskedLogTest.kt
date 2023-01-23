package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import org.junit.jupiter.api.Test

class MaskedLogTest {

    @Test
    fun manuellTest() {
        System.setProperty("logback.configurationFile", "logback.xml")
        val LOG = KotlinLogging.logger {}
        LOG.info("12345678901")
        LOG.info("1234567890123")
        LOG.info("12345678901er")
        LOG.info("e12345678901e")
    }
}
