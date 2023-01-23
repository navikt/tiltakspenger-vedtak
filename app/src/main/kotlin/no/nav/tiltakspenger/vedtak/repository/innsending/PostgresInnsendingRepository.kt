package no.nav.tiltakspenger.vedtak.repository.innsending

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.nå
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.InnsendingTilstandType
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.repository.aktivitetslogg.AktivitetsloggDAO
import no.nav.tiltakspenger.vedtak.repository.personopplysninger.PersonopplysningerDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet.TiltaksaktivitetDAO
import no.nav.tiltakspenger.vedtak.repository.ytelse.YtelsesakDAO
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

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

    override fun antall(): Long {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(antall).map { row ->
                        row.toAntall()
                    }.asSingle
                )
            }
        }!!
    }

    override fun hentInnsendingerMedTilstandFaktainnhentingFeilet(): List<String> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(hentMedTilstand, InnsendingTilstandType.FaktainnhentingFeilet.name).map { row ->
                        // row.toInnsending(txSession)
                        row.toJournalpostId()
                    }.asList
                )
            }
        }
    }

    override fun hentInnsendingerMedTilstandFerdigstilt(): List<String> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(hentMedTilstand, InnsendingTilstandType.InnsendingFerdigstilt.name).map { row ->
                        // row.toInnsending(txSession)
                        row.toJournalpostId()
                    }.asList
                )
            }
        }
    }

    override fun hentInnsendingerStoppetUnderBehandling(): List<String> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        hentStoppetUnderBehandling,
                        mapOf(
                            "tilstand1" to InnsendingTilstandType.FaktainnhentingFeilet.name,
                            "tilstand2" to InnsendingTilstandType.InnsendingFerdigstilt.name,
                            "sistEndret" to LocalDateTime.now().minusDays(1),
                        )
                    ).map { row ->
                        // row.toInnsending(txSession)
                        row.toJournalpostId()
                    }.asList
                )
            }
        }
    }

    override fun antallMedTilstandFaktainnhentingFeilet(): Long {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(antallMedTilstand, InnsendingTilstandType.FaktainnhentingFeilet.name).map { row ->
                        row.toAntall()
                    }.asSingle
                )
            }
        }!!
    }

    override fun antallStoppetUnderBehandling(): Long {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        antallStoppetUnderBehandling,
                        mapOf(
                            "tilstand1" to InnsendingTilstandType.FaktainnhentingFeilet.name,
                            "tilstand2" to InnsendingTilstandType.InnsendingFerdigstilt.name,
                            "sistEndret" to LocalDateTime.now().minusDays(1),
                        )
                    ).map { row ->
                        row.toAntall()
                    }.asSingle
                )
            }
        }!!
    }

    override fun lagre(innsending: Innsending): Innsending {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                if (innsendingFinnes(journalpostId = innsending.journalpostId, txSession = txSession)) {
                    oppdaterTilstand(innsending = innsending, txSession = txSession)
                } else {
                    insert(innsending = innsending, txSession = txSession)
                }.also {
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
    }

    override fun findBySøknadId(søknadId: String): Innsending? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return søknadDAO.finnJournalpostId(søknadId, txSession)
                    ?.let { journalpostId -> this.hentMedTxSession(journalpostId, txSession) }
            }
        }
    }

    private fun Row.toAntall(): Long {
        return long("antall")
    }

    private fun Row.toJournalpostId(): String = string("journalpost_id")

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

    private fun insert(innsending: Innsending, txSession: TransactionalSession): Innsending {
        LOG.info { "Insert innsending" }
        SECURELOG.info { "Insert innsending ${innsending.id}" }
        val nå = nå()
        innsending.oppdaterSistEndret(nå)
        txSession.run(
            queryOf(
                lagre,
                mapOf(
                    "id" to innsending.id.toString(),
                    "journalpostId" to innsending.journalpostId,
                    "ident" to innsending.ident,
                    "tilstand" to innsending.tilstand.type.name,
                    "sist_endret" to innsending.sistEndret,
                    "opprettet" to nå,
                )
            ).asUpdate
        )
        return innsending
    }

    private fun oppdaterTilstand(innsending: Innsending, txSession: TransactionalSession): Innsending {
        LOG.info { "Update innsending" }
        SECURELOG.info { "Update innsending ${innsending.id} tilstand ${innsending.tilstand}" }
        val sistEndretOld = innsending.sistEndret
        innsending.oppdaterSistEndret(nå())

        val antRaderOppdatert = txSession.run(
            queryOf(
                oppdater,
                mapOf(
                    "id" to innsending.id.toString(),
                    "tilstand" to innsending.tilstand.type.name,
                    "sistEndretOld" to sistEndretOld,
                    "sistEndret" to innsending.sistEndret,
                )
            ).asUpdate
        )
        if (antRaderOppdatert == 0) {
            throw IllegalStateException("Noen andre har endret denne")
        }
        return innsending
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
    private val antall = "select count(id) as antall from innsending"

    @Language("SQL")
    private val antallMedTilstand = "select count(id) as antall from innsending where tilstand = ?"

    @Language("SQL")
    private val hentMedTilstand = "select * from innsending where tilstand = ?"

    @Language("SQL")
    private val antallStoppetUnderBehandling =
        "select count(id) as antall from innsending where tilstand not in (:tilstand1, :tilstand2) and sist_endret < :sistEndret"

    @Language("SQL")
    private val hentStoppetUnderBehandling =
        "select * from innsending where tilstand not in (:tilstand1, :tilstand2) and sist_endret < :sistEndret"

    @Language("SQL")
    private val findByIdent = "select * from innsending where ident = ?"
}
