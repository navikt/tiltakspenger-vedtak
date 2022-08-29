package no.nav.tiltakspenger.vedtak.repository

import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource.session
import org.intellij.lang.annotations.Language

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

object PostgresSøkerRepository : SøkerRepository {

    @Language("SQL")
    private val lagre = "insert into søker (id, ident, tilstand) values (:id, :ident, :tilstand)"

    @Language("SQL")
    private val finnes = "select exists(select 1 from søker where ident=:ident)"

    override fun hent(ident: String): Søker? {
        TODO("Not yet implemented")
    }

    private fun brukerFinnes(ident: String): Boolean = session.run(
        queryOf(finnes, mapOf("ident" to ident)).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw InternalError("Failed to check if person exists")

    override fun lagre(søker: Søker): Int {
        val søkerDto = SøkerDto.fromSøker(søker)
        return if (brukerFinnes(søkerDto.ident)) {
            LOG.info { "User already exists" }
            SECURELOG.info { "User ${søkerDto.id} already exists" }
            0
        } else {
            LOG.info { "Insert user" }
            SECURELOG.info { "Insert user ${søkerDto.id}" }
            session.run(
                queryOf(lagre, mapOf("id" to søkerDto.id, "ident" to søkerDto.ident, "tilstand" to søkerDto.tilstand))
                    .asUpdate
            )
        }
    }
}
