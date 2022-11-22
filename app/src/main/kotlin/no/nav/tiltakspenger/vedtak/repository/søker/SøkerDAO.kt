package no.nav.tiltakspenger.vedtak.repository.søker

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository
import org.intellij.lang.annotations.Language

internal class SøkerDAO(
    private val innsendingRepository: PostgresInnsendingRepository = PostgresInnsendingRepository(),
) {
    fun hent(søkerId: SøkerId): Søker? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return txSession.run(
                    queryOf(hent, søkerId).map { row ->
                        row.toSøker(txSession)
                    }.asSingle
                )
            }
        }
    }

    fun lagre(søker: Søker) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                if (søkerFinnes(søker.søkerId, txSession)) {
                    when (søker) {
                        is Søker.Init -> throw IllegalStateException("Kan ikke oppdatere en søker i Init state")
                        is Søker.Opprettet -> oppdaterSøker(søker, txSession)
                    }
                } else {
                    when (søker) {
                        is Søker.Init -> lagreInitSøker(søker, txSession)
                        is Søker.Opprettet -> lagreSøker(søker, txSession)
                    }
                }
                wh
            }
        }
    }

    //    søknad?.let { lagreHeleSøknaden(innsendingId, it, txSession) } //TODO: Burde vel egentlig slette søknaden..
    private fun søkerFinnes(søkerId: SøkerId, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, søkerId.toString()).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw RuntimeException("Failed to check if søker exists")

    private fun oppdaterSøker(søker: Søker.Opprettet, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                oppdaterSøker, mapOf(
                    "id" to søker.søkerId.toString(),
                    "ident" to søker.ident,
                    "fornavn" to søker.fornavn,
                    "mellomnavn" to søker.mellomnavn,
                    "etternavn" to søker.etternavn,
                    "skjermet" to søker.skjermet,
                    "fortrolig" to søker.fortrolig,
                    "strengtFortrolig" to søker.strengtFortrolig,
                    "strengtFortroligUtland" to søker.strengtFortroligUtland,
                    "opprettet" to søker.opprettet,
                    "tidsstempel" to søker.tidsstempel,
                )
            ).asUpdate
        )
    }

    private fun lagreInitSøker(søker: Søker, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                lagreInitSøker, mapOf(
                    "id" to søker.søkerId.toString(),
                    "ident" to søker.ident,
                    "opprettet" to søker.opprettet,
                    "tidsstempel" to søker.tidsstempel,
                )
            ).asUpdate
        )
    }

    private fun lagreSøker(søker: Søker.Opprettet, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                lagreInitSøker, mapOf(
                    "id" to søker.søkerId.toString(),
                    "ident" to søker.ident,
                    "fornavn" to søker.fornavn,
                    "mellomnavn" to søker.mellomnavn,
                    "etternavn" to søker.etternavn,
                    "fortrolig" to søker.fortrolig,
                    "strengtFortrolig" to søker.strengtFortrolig,
                    "strengtFortroligUtland" to søker.strengtFortroligUtland,
                    "opprettet" to søker.opprettet,
                    "tidsstempel" to søker.tidsstempel,
                )
            ).asUpdate
        )
    }

    private fun Row.toSøker(txSession: TransactionalSession): Søker {
        val id = SøkerId.fromDb(string("id"))
        val ident = string("ident")

        return Søker.Init(
            søkerId = id,
            ident = ident,
        )
    }

    @Language("SQL")
    private val lagreInitSøker = """
        insert into søker (
            id,
            ident,
            tidsstempel,
            opprettet
        ) values (
            :id, 
            :ident,
            :tidsstempel,
            :opprettet
        )""".trimIndent()

    @Language("SQL")
    private val lagreSøker = """
        insert into søker (
            id,
            ident,
            fornavn,
            mellomnavn,
            etternavn,
            fortrolig,
            strengtFortrolig,
            strengtFortroligUtland,
            tidsstempel,
            opprettet
        ) values (
            :id, 
            :ident,
            :fornavn,
            :mellomnavn,
            :etternavn,
            :fortrolig,
            :strengtFortrolig,
            :strengtFortroligUtland,
            :tidsstempel,
            :opprettet
        )""".trimIndent()

    @Language("SQL")
    private val oppdaterSøker = """
        update søker set  
            ident = :ident,
            fornavn = :fornavn, 
            mellomnavn = :mellomnavn, 
            etternavn = :etternavn, 
            skjermet = :skjermet, 
            fortrolig = :fortrolig,
            strengtFortrolig = :strengtFortrolig,
            strengtFortroligUtland = :strengtFortroligUtland,
            tidsstempel = :tidsstempel
        where id = :id
        """.trimIndent()

    @Language("SQL")
    private val finnes = "select exists(select 1 from søker where id = ?)"

    @Language("SQL")
    private val hent = "select * from søker where id = ?"
}
