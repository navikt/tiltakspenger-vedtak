package no.nav.tiltakspenger.vedtak.repository.innsending

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.nå
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.repository.aktivitetslogg.AktivitetsloggDAO
import no.nav.tiltakspenger.vedtak.repository.personopplysninger.PersonopplysningerDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet.TiltaksaktivitetDAO
import no.nav.tiltakspenger.vedtak.repository.ytelse.YtelsesakDAO
import org.intellij.lang.annotations.Language
import java.sql.SQLException

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PostgresInnsendingRepository(
    private val søknadDAO: SøknadDAO = SøknadDAO(),
    private val tiltaksaktivitetDAO: TiltaksaktivitetDAO = TiltaksaktivitetDAO(),
    private val personopplysningerDAO: PersonopplysningerDAO = PersonopplysningerDAO(),
    private val ytelsesakDAO: YtelsesakDAO = YtelsesakDAO(),
    private val aktivitetsloggDAO: AktivitetsloggDAO = AktivitetsloggDAO(),
) : InnsendingRepository {

    override fun hent(journalpostId: String): Innsending? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return hentMedTxSession(journalpostId, txSession)
            }
        }
    }

    private fun hentMedTxSession(
        journalpostId: String,
        txSession: TransactionalSession
    ): Innsending? {
        return txSession.run(
            queryOf(hent, journalpostId).map { row ->
                row.toInnsending(txSession)
            }.asSingle
        )
    }

    override fun findByIdent(ident: String): List<Innsending> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(findByIdent, ident).map { row ->
                        row.toInnsending(txSession)
                    }.asList
                )
            }
        }
    }

    override fun lagre(innsending: Innsending) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                if (innsendingFinnes(journalpostId = innsending.journalpostId, txSession = txSession)) {
                    oppdater(innsending = innsending, txSession = txSession)
                } else {
                    insert(innsending = innsending, txSession = txSession)
                }
                søknadDAO.lagre(innsendingId = innsending.id, søknad = innsending.søknad, txSession = txSession)
                tiltaksaktivitetDAO.lagre(
                    innsendingId = innsending.id,
                    tiltaksaktiviteter = innsending.tiltak,
                    txSession = txSession
                )
                ytelsesakDAO.lagre(
                    innsendingId = innsending.id,
                    ytelsesaker = innsending.ytelser,
                    txSession = txSession
                )
                personopplysningerDAO.lagre(
                    innsendingId = innsending.id,
                    personopplysninger = innsending.personopplysninger,
                    txSession = txSession
                )
                aktivitetsloggDAO.lagre(
                    innsendingId = innsending.id,
                    aktivitetslogg = innsending.aktivitetslogg,
                    txSession = txSession
                )
            }
        }
    }

    override fun findBySøknadId(søknadId: String): Innsending? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return søknadDAO.finnJournalpostId(søknadId, txSession)
                    ?.let { journalpostId -> this.hentMedTxSession(journalpostId, txSession) }
            }
        }
    }

    private fun Row.toInnsending(txSession: TransactionalSession): Innsending {
        val id = InnsendingId.fromDb(string("id"))
        return Innsending.fromDb(
            id = id,
            journalpostId = string("journalpost_id"),
            ident = string("ident"),
            tilstand = string("tilstand"),
            sistEndret = localDateTime("sist_endret"),
            søknad = søknadDAO.hent(id, txSession),
            tiltak = tiltaksaktivitetDAO.hentForInnsending(id, txSession),
            ytelser = ytelsesakDAO.hentForInnsending(id, txSession),
            personopplysninger = personopplysningerDAO.hent(id, txSession),
            aktivitetslogg = aktivitetsloggDAO.hent(id, txSession)
        )
    }

    private fun innsendingFinnes(journalpostId: String, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, journalpostId).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw RuntimeException("Failed to check if innsending exists")

    private fun insert(innsending: Innsending, txSession: TransactionalSession) {
        LOG.info { "Insert innsending" }
        SECURELOG.info { "Insert innsending ${innsending.id}" }
        val nå = nå()
        txSession.run(
            queryOf(
                lagre,
                mapOf(
                    "id" to innsending.id.toString(),
                    "journalpostId" to innsending.journalpostId,
                    "ident" to innsending.ident,
                    "tilstand" to innsending.tilstand.type.name,
                    "sist_endret" to nå,
                    "opprettet" to nå,
                )
            ).asUpdate
        )
    }

    private fun oppdater(innsending: Innsending, txSession: TransactionalSession) {
        LOG.info { "Update innsending" }
        SECURELOG.info { "Update innsending ${innsending.id} tilstand ${innsending.tilstand}" }
        val antRaderOppdatert = txSession.run(
            queryOf(
                oppdater,
                mapOf(
                    "id" to innsending.id.toString(),
                    "tilstand" to innsending.tilstand.type.name,
                    "sistEndretOld" to innsending.sistEndret,
                    "sistEndret" to nå(),
                )
            ).asUpdate
        )
        if (antRaderOppdatert == 0 ) {
            throw IllegalStateException("Noen andre har endret denne")
        }
    }

    @Language("SQL")
    private val lagre =
        "insert into innsending (id, journalpost_id, ident, tilstand, sist_endret, opprettet) values (:id, :journalpostId, :ident, :tilstand, :sist_endret, :opprettet)"

    @Language("SQL")
    private val oppdater =
        """update innsending set 
              tilstand = :tilstand,
              sist_endret = :sistEndret
           where id = :id
             and sist_endret = :sistEndretOld
        """.trimMargin()

    @Language("SQL")
    private val finnes = "select exists(select 1 from innsending where journalpost_id = ?)"

    @Language("SQL")
    private val hent = "select * from innsending where journalpost_id = ?"

    @Language("SQL")
    private val findByIdent = "select * from innsending where ident = ?"
}
