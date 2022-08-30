package no.nav.tiltakspenger.vedtak.repository

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource.session
import no.nav.tiltakspenger.vedtak.db.hent
import org.intellij.lang.annotations.Language

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

object PostgresSøkerRepository : SøkerRepository {

    @Language("SQL")
    private val lagre = "insert into søker (id, ident, tilstand) values (:id, :ident, :tilstand)"

    @Language("SQL")
    private val finnes = "select exists(select 1 from søker where ident = ?)"

    @Language("SQL")
    private val hent = "select * from søker where ident = ?"

    fun hentSøker(ident: String, session: Session): Søker? =
        "select * from søker where ident=:ident"
            .hent(mapOf("ident" to ident), session) {
                it.toSøkerDto().let { søkerDto ->
                    Søker.fromDb(
                        id = søkerDto.id,
                        ident = søkerDto.ident,
                        tilstand = søkerDto.tilstand,
                    )
                }
            }

    override fun hent(ident: String): Søker? {
        val søkerDto: SøkerDto = session.run(
            queryOf(hent, ident).map { row ->
                row.toSøkerDto()
            }.asSingle
        ) ?: return null
        return Søker.fromDb(
            id = søkerDto.id,
            ident = søkerDto.ident,
            tilstand = søkerDto.tilstand,
        )
    }

    private fun Row.toSøkerDto(): SøkerDto {
        val ident = string("ident")
        val id = uuid("id")
        val tilstand = string("tilstand")
        return SøkerDto(
            id = id,
            ident = ident,
            tilstand = tilstand,
        )
    }

    private fun brukerFinnes(ident: String): Boolean = session.run(
        queryOf(finnes, ident).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw InternalError("Failed to check if person exists")

    override fun lagre(søker: Søker): Int {
        val søkerDto = SøkerDto.fromSøker(søker)
        if (brukerFinnes(søkerDto.ident)) {
            LOG.info { "User already exists" }
            SECURELOG.info { "User ${søkerDto.id} already exists" }
            return 0
        }
        LOG.info { "Insert user" }
        SECURELOG.info { "Insert user ${søkerDto.id}" }
        return session.run(
            queryOf(
                lagre,
                mapOf(
                    "id" to søkerDto.id,
                    "ident" to søkerDto.ident,
                    "tilstand" to søkerDto.tilstand
                )
            ).asUpdate
        )
    }
}
