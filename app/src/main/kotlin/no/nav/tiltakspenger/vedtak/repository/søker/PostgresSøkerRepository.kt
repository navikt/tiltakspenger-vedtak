package no.nav.tiltakspenger.vedtak.repository.søker

import java.time.LocalDateTime
import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import org.intellij.lang.annotations.Language

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PostgresSøkerRepository(
    private val søknadDAO: SøknadDAO,
) : SøkerRepository {

    override fun hent(ident: String): Søker? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return txSession.run(
                    queryOf(hent, ident).map { row ->
                        row.toSøker()
                    }.asSingle
                )
            }
        }
    }

    override fun lagre(søker: Søker) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                if (brukerFinnes(søker.ident, txSession)) oppdaterTilstand(søker, txSession) else insert(
                    søker,
                    txSession
                )
                søknadDAO.lagre(søker.id, søker.søknader, txSession)
            }
        }
    }

    private fun Row.toSøker(): Søker {
        val ident = string("ident")
        val id = uuid("id")
        val tilstand = string("tilstand")
        return Søker.fromDb(
            id = id,
            ident = ident,
            tilstand = tilstand,
            søknader = emptyList(), // søknadDAO.hentAlle(ident)
        )
    }

    private fun brukerFinnes(ident: String, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, ident).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw RuntimeException("Failed to check if person exists")

    private fun insert(søker: Søker, txSession: TransactionalSession) {
        LOG.info { "Insert user" }
        SECURELOG.info { "Insert user ${søker.id}" }
        txSession.run(
            queryOf(
                lagre,
                mapOf(
                    "id" to søker.id,
                    "ident" to søker.ident,
                    "tilstand" to søker.tilstand.type.toString(),
                    "sist_endret" to LocalDateTime.now(),
                    "opprettet" to LocalDateTime.now(),
                )
            ).asUpdate
        )
    }

    private fun oppdaterTilstand(søker: Søker, txSession: TransactionalSession) {
        LOG.info { "Update user" }
        SECURELOG.info { "Update user ${søker.id} tilstand ${søker.tilstand}" }
        txSession.run(
            queryOf(
                oppdater,
                mapOf(
                    "id" to søker.id,
                    "tilstand" to søker.tilstand.toString(),
                    "sistEndret" to LocalDateTime.now()
                )
            ).asUpdate
        )
    }

    @Language("SQL")
    private val lagre =
        "insert into søker (id, ident, tilstand, sist_endret, opprettet) values (:id, :ident, :tilstand, :sist_endret, :opprettet)"

    @Language("SQL")
    private val oppdater =
        """update søker set 
              tilstand = :tilstand, 
              sist_endret = :sistEndret
           where id = :id
        """.trimMargin()

    @Language("SQL")
    private val finnes = "select exists(select 1 from søker where ident = ?)"

    @Language("SQL")
    private val hent = "select * from søker where ident = ?"
}
