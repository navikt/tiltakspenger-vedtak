package no.nav.tiltakspenger.vedtak.repository.søker

import java.time.LocalDateTime
import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource.session
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadRepository
import org.intellij.lang.annotations.Language

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PostgresSøkerRepository(
    private val søknadRepository: SøknadRepository,
) : SøkerRepository {
    fun hentSøker(ident: String, session: Session): Søker? {
        return session.run(
            queryOf(hent, ident).map { row ->
                row.toSøker()
            }.asSingle
        )
    }


    override fun hent(ident: String): Søker? {
        return session.run(
            queryOf(hent, ident).map { row ->
                row.toSøker()
            }.asSingle
        )
    }

    private fun Row.toSøker(): Søker {
        val ident = string("ident")
        val id = uuid("id")
        val tilstand = string("tilstand")
        return Søker.fromDb(
            id = id,
            ident = ident,
            tilstand = tilstand,
            søknader = emptyList(), // søknadRepository.hentAlle(ident)
        )
    }

    private fun brukerFinnes(ident: String): Boolean = session.run(
        queryOf(finnes, ident).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw InternalError("Failed to check if person exists")

    override fun lagre(søker: Søker): Int {
        var antall = if (brukerFinnes(søker.ident)) oppdaterTilstand(søker) else insert(søker)

        antall += søknadRepository.lagre(søker.ident, søker.søknader)
        return antall
    }

    private fun insert(søker: Søker): Int {
        LOG.info { "Insert user" }
        SECURELOG.info { "Insert user ${søker.id}" }
        return session.run(
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

    private fun oppdaterTilstand(søker: Søker): Int {
        LOG.info { "Update user" }
        SECURELOG.info { "Update user ${søker.id} tilstand ${søker.tilstand}" }
        return session.run(
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
