package no.nav.tiltakspenger.vedtak.repository.søker

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.innsending.Søker
import org.intellij.lang.annotations.Language

class SøkerRepositoryImpl(
    private val personopplysningerDAO: PersonopplysningerDAO = PersonopplysningerDAO(),
) : SøkerRepository {

    override fun findByIdent(ident: String): Søker? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return txSession.run(
                    queryOf(findByIdent, ident).map { row ->
                        row.toSøker(txSession)
                    }.asSingle,
                )
            }
        }
    }

    override fun hent(søkerId: SøkerId): Søker? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return txSession.run(
                    queryOf(hent, søkerId.toString()).map { row ->
                        row.toSøker(txSession)
                    }.asSingle,
                )
            }
        }
    }

    override fun lagre(søker: Søker) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                if (søkerFinnes(søker.søkerId, txSession)) {
                    oppdaterSøker(søker, txSession)
                } else {
                    lagreSøker(søker, txSession)
                }
                personopplysningerDAO.lagre(
                    søkerId = søker.søkerId,
                    personopplysninger = søker.personopplysninger,
                    txSession = txSession,
                )
            }
        }
    }

    private fun søkerFinnes(søkerId: SøkerId, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, søkerId.toString()).map { row -> row.boolean("exists") }.asSingle,
    ) ?: throw RuntimeException("Failed to check if søker exists")

    private fun oppdaterSøker(søker: Søker, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                oppdaterSøker,
                mapOf(
                    "ident" to søker.ident,
                    "sistEndret" to nå(),
                ),
            ).asUpdate,
        )
    }

    private fun lagreSøker(søker: Søker, txSession: TransactionalSession) {
        val nå = nå()
        txSession.run(
            queryOf(
                lagreSøker,
                mapOf(
                    "id" to søker.søkerId.toString(),
                    "ident" to søker.ident,
                    "opprettet" to nå,
                    "sistEndret" to nå,
                ),
            ).asUpdate,
        )
    }

    private fun Row.toSøker(txSession: TransactionalSession): Søker {
        val id = SøkerId.fromString(string("id"))
        val ident = string("ident")
        val personopplysninger = personopplysningerDAO.hent(id, txSession)
        return Søker.fromDb(
            søkerId = id,
            ident = ident,
            personopplysninger = personopplysninger,
        )
    }

    @Language("SQL")
    private val lagreSøker = """
        insert into søker (
            id,
            ident,
            sist_endret,
            opprettet
        ) values (
            :id, 
            :ident,
            :sistEndret,
            :opprettet
        )
    """.trimIndent()

    @Language("SQL")
    private val oppdaterSøker = """
        update søker set  
            ident = :ident,
            sist_endret = :sistEndret
        where id = :id
    """.trimIndent()

    @Language("SQL")
    private val finnes = "select exists(select 1 from søker where id = ?)"

    @Language("SQL")
    private val hent = "select * from søker where id = ?"

    @Language("SQL")
    private val findByIdent = "select * from søker where ident = ?"
}
