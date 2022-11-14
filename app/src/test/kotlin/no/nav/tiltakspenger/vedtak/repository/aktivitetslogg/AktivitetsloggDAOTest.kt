package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import kotliquery.sessionOf
import no.nav.tiltakspenger.objectmothers.innsendingRegistrert
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Kontekst
import no.nav.tiltakspenger.vedtak.KontekstLogable
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresInnsendingRepository
import org.junit.jupiter.api.BeforeEach
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
        val søker = innsendingRegistrert()
        PostgresInnsendingRepository().lagre(søker)

        val aktivitetslogg = Aktivitetslogg()
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

        hentetAktivitetslogg shouldBeEqualToComparingFields aktivitetslogg
    }

    @Test
    fun `skal kunne hente en aktivitetslogg med flere aktiviteter`() {
        val søker = innsendingRegistrert()
        PostgresInnsendingRepository().lagre(søker)

        val aktivitetslogg = Aktivitetslogg()
        aktivitetslogg.info("en liten melding")
        aktivitetslogg.warn("en warn melding til")
        aktivitetslogg.info("en warn melding til1")
        aktivitetslogg.warn("en warn melding til2")
        aktivitetslogg.info("en warn melding til3")
        aktivitetslogg.warn("en warn melding til4")
        aktivitetslogg.warn("en warn melding til5")
        aktivitetslogg.warn("en warn melding til6")
        aktivitetslogg.warn("en warn melding til7")

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

        hentetAktivitetslogg.aktiviteter shouldContainExactly aktivitetslogg.aktiviteter
    }

    @Test
    fun `skal kunne lagre og hente en aktivitetslogg flere ganger og legge til nye aktiviteter, men få riktig rekkefølge`() {
        val søker = innsendingRegistrert()
        PostgresInnsendingRepository().lagre(søker)

        val aktivitetslogg = Aktivitetslogg()
        aktivitetslogg.info("en liten melding")
        aktivitetslogg.warn("en warn melding til")
        aktivitetslogg.info("en warn melding til1")
        aktivitetslogg.warn("en warn melding til2")

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

        hentetAktivitetslogg.info("en warn melding til3")
        hentetAktivitetslogg.warn("en warn melding til4")
        hentetAktivitetslogg.warn("en warn melding til5")
        hentetAktivitetslogg.warn("en warn melding til6")
        hentetAktivitetslogg.warn("en warn melding til7")

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, hentetAktivitetslogg, txSession)
            }
        }

        val hentetAktivitetslogg2 = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(søker.id, txSession)
            }
        }

        hentetAktivitetslogg2.aktiviteter shouldContainExactly hentetAktivitetslogg.aktiviteter
    }

    @Test
    fun `skal kunne lagre behov`() {
        val søker = innsendingRegistrert()
        PostgresInnsendingRepository().lagre(søker)

        val aktivitetslogg = Aktivitetslogg()
        aktivitetslogg.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.personopplysninger,
            melding = "melding",
            detaljer = mapOf(
                "ident" to "1234",
                "tall" to 32,
            )
        )

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

        hentetAktivitetslogg shouldBeEqualToComparingFields aktivitetslogg
    }

    @Test
    fun `skal kunne lagre og hente kontekster`() {
        val søker = innsendingRegistrert()
        PostgresInnsendingRepository().lagre(søker)

        val aktivitetslogg = Aktivitetslogg()
        aktivitetslogg.addKontekst(søker)
        aktivitetslogg.addKontekst(object : KontekstLogable {
            override fun opprettKontekst() = Kontekst("testType", mapOf("foo" to "bar"))
        })
        aktivitetslogg.info("en melding")

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

        hentetAktivitetslogg shouldBeEqualToComparingFields aktivitetslogg
    }
}
