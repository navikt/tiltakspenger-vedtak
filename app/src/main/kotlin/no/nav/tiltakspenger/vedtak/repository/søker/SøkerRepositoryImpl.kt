package no.nav.tiltakspenger.vedtak.repository.søker

import kotliquery.Row
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker
import no.nav.tiltakspenger.saksbehandling.ports.SøkerRepository
import org.intellij.lang.annotations.Language

class SøkerRepositoryImpl(
    private val sessionFactory: PostgresSessionFactory,
    private val personopplysningerDAO: PersonopplysningerDAO,
) : SøkerRepository {

    override fun findByIdent(ident: String, sessionContext: SessionContext?): Søker? {
        return sessionFactory.withSession(sessionContext) { session ->
            session.run(
                queryOf(findByIdent, ident).map { row ->
                    row.toSøker(session)
                }.asSingle,
            )
        }
    }

    override fun hent(søkerId: SøkerId): Søker? {
        return sessionFactory.withTransaction { tx ->
            tx.run(
                queryOf(hent, søkerId.toString()).map { row ->
                    row.toSøker(tx)
                }.asSingle,
            )
        }
    }

    override fun lagre(søker: Søker, transactionContext: TransactionContext?) {
        sessionFactory.withTransaction(transactionContext) { tx ->
            if (søkerFinnes(søker.søkerId, tx)) {
                oppdaterSøker(søker, tx)
            } else {
                lagreSøker(søker, tx)
            }
            personopplysningerDAO.lagre(
                søkerId = søker.søkerId,
                personopplysninger = søker.personopplysninger,
                txSession = tx,
            )
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

    private fun Row.toSøker(session: Session): Søker {
        val id = SøkerId.fromString(string("id"))
        val ident = string("ident")
        val personopplysninger = personopplysningerDAO.hent(id, session)
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
