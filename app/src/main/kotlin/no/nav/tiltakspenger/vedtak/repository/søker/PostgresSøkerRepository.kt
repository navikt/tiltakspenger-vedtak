package no.nav.tiltakspenger.vedtak.repository.søker

import java.time.LocalDateTime
import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource.session
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import org.intellij.lang.annotations.Language

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PostgresSøkerRepository(
    private val søknadDAO: SøknadDAO,
) : SøkerRepository {

    @Language("SQL")
    private val lagre =
        "insert into søker (id, ident, tilstand, sist_endret, opprettet) values (:id, :ident, :tilstand, :sist_endret, :opprettet)"

    @Language("SQL")
    private val finnes = "select exists(select 1 from søker where ident = ?)"

    @Language("SQL")
    private val hent = "select * from søker where ident = ?"

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
        if (brukerFinnes(søker.ident)) {
            LOG.info { "User already exists" }
            SECURELOG.info { "User ${søker.id} already exists" }
            return 0
        }
        LOG.info { "Insert user" }
        SECURELOG.info { "Insert user ${søker.id}" }
//        søknadRepository.lagre(søker.søknader)
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

    override fun oppdaterTilstand(tilstand: Søker.Tilstand) {
        TODO("Not yet implemented")
    }
}
