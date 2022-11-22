package no.nav.tiltakspenger.vedtak.repository.søker

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource
import org.intellij.lang.annotations.Language

internal class SøkerRepository {
    fun hent(søkerId: SøkerId): Søker? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return txSession.run(
                    queryOf(hent, søkerId).map { row ->
                        row.toSøker()
                    }.asSingle
                )
            }
        }
    }

    fun lagre(søker: Søker) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                if (søkerFinnes(søker.søkerId, txSession)) {
                    oppdaterSøker(søker, txSession)
                } else {
                    lagreInitSøker(søker, txSession)
                }
            }
        }
    }

    private fun søkerFinnes(søkerId: SøkerId, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, søkerId.toString()).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw RuntimeException("Failed to check if søker exists")

    private fun oppdaterSøker(søker: Søker, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                oppdaterSøker, mapOf(
                    "ident" to søker.ident,
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


    private fun Row.toSøker(): Søker {
        val id = SøkerId.fromDb(string("id"))
        val ident = string("ident")
        val tidsstempel = localDateTime("tidsstempel")
        val opprettet = localDateTime("opprettet")
        return Søker(
            søkerId = id,
            ident = ident,
            tidsstempel = tidsstempel,
            opprettet = opprettet,
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
    private val oppdaterSøker = """
        update søker set  
            ident = :ident,
            tidsstempel = :tidsstempel
        where id = :id
        """.trimIndent()

    @Language("SQL")
    private val finnes = "select exists(select 1 from søker where id = ?)"

    @Language("SQL")
    private val hent = "select * from søker where id = ?"
}
