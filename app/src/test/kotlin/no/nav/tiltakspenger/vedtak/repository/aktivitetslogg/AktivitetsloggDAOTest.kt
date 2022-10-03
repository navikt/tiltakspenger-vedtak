package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.db.DataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*

@Disabled
internal class AktivitetsloggDAOTest {

    @Test
    fun `skal kunne lagre`() {

        val søkerId = UUID.randomUUID()
        val aktivitetslogg = Aktivitetslogg()
        val dao = AktivitetsloggDAO()

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søkerId, aktivitetslogg, txSession)
            }
        }

        val hentetAktivitetslogg = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(søkerId, txSession)
            }
        }

        assertEquals(aktivitetslogg, hentetAktivitetslogg)
    }
}
