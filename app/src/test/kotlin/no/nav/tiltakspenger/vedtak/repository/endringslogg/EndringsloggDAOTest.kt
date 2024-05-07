package no.nav.tiltakspenger.vedtak.repository.endringslogg

import io.kotest.matchers.collections.shouldContainExactly
import kotliquery.sessionOf
import no.nav.tiltakspenger.objectmothers.ObjectMother.tomSak
import no.nav.tiltakspenger.saksbehandling.domene.endringslogg.Endringslogg
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class EndringsloggDAOTest {

    companion object {
        @Container
        val testcontainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `skal kunne lagre og hente opp`() {
        val sak = tomSak()
        PostgresSakRepo().lagre(sak)

        val endringslogg = Endringslogg(sak.id)
        endringslogg.info("en liten melding", null)

        val dao = EndringsloggDAO()

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(sak.id, null, endringslogg, txSession)
            }
        }

        val hentetEndringer = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(sak.id, txSession)
            }
        }

        hentetEndringer shouldContainExactly endringslogg.endringsliste
    }
}
