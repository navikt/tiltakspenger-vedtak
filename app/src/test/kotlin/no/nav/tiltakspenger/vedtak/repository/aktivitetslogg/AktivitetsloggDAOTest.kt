package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import kotliquery.sessionOf
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.AktivitetsloggVisitor
import no.nav.tiltakspenger.vedtak.Kontekst
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.objectmothers.søkerRegistrert
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

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

        class PublicAktiviteter(
            val kontekster: List<Kontekst>,
            val aktivitet: Aktivitetslogg.Aktivitet,
            val melding: String,
            val tidsstempel: LocalDateTime,
        )

        class aktVis() : AktivitetsloggVisitor {
            val aktiviteter = mutableListOf<PublicAktiviteter>()

            override fun visitInfo(
                kontekster: List<Kontekst>,
                aktivitet: Aktivitetslogg.Aktivitet.Info,
                melding: String,
                tidsstempel: LocalDateTime
            ) {
                aktiviteter.add(
                    PublicAktiviteter(
                        kontekster = kontekster,
                        aktivitet = aktivitet,
                        melding = melding,
                        tidsstempel = tidsstempel,
                    )
                )
            }
        }

        val vis = aktVis()
        aktivitetslogg.accept(vis)


//        class Visitor : AktivitetsloggVisitor {
//            override fun visitInfo(
//                kontekster: List<Kontekst>,
//                aktivitet: Aktivitetslogg.Aktivitet.Info,
//                melding: String,
//                tidsstempel: LocalDateTime
//            )
//        }
//        aktivitetslogg


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
