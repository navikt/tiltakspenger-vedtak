package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.objectmothers.søkerRegistrert
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class AktivitetsloggDAOTest {

    companion object {
        @Container
        val testcontainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `skal kunne lagre`() {
        val søker = søkerRegistrert()
        val aktivitetslogg = Aktivitetslogg()
        aktivitetslogg.addKontekst(søker)
        aktivitetslogg.info("en liten melding")
        val dao = AktivitetsloggDAO()

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, aktivitetslogg, txSession)
            }
        }

        val hentetAktivitetslogg = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(søker.id, txSession)
            }
        }

        assertEquals(aktivitetslogg, hentetAktivitetslogg)
    }
}
