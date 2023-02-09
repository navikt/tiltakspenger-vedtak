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
import no.nav.tiltakspenger.vedtak.repository.foreldrepenger.ForeldrepengerVedtakDAO
import no.nav.tiltakspenger.vedtak.repository.overgangsstønad.OvergangsstønadVedtakDAO
import no.nav.tiltakspenger.vedtak.repository.personopplysninger.PersonopplysningerDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet.TiltaksaktivitetDAO
import no.nav.tiltakspenger.vedtak.repository.uføre.UføreVedtakDAO
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
    private val foreldrepengerVedtakDAO: ForeldrepengerVedtakDAO = ForeldrepengerVedtakDAO(),
    private val overgangsstønadVedtakDAO: OvergangsstønadVedtakDAO = OvergangsstønadVedtakDAO(),
    private val uføreVedtakDAO: UføreVedtakDAO = UføreVedtakDAO(),
    private val aktivitetsloggDAO: AktivitetsloggDAO = AktivitetsloggDAO(),
) : InnsendingRepository {

    override fun hent(journalpostId: String): Innsending? {
        val start = System.currentTimeMillis()
        val returnValue = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                hentMedTxSession(journalpostId, txSession)
            }
        }
        LOG.info { "PostgresInnsendingRepository.hent tok ${(System.currentTimeMillis() - start)} ms" }
        return returnValue
    }

    private fun hentMedTxSession(
        journalpostId: String,
        txSession: TransactionalSession,
    ): Innsending? {
        return txSession.run(
            queryOf(hent, journalpostId).map { row ->
                row.toInnsending(txSession)
            }.asSingle,
        )
    }

    override fun findByIdent(ident: String): List<Innsending> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(findByIdent, ident).map { row ->
                        row.toInnsending(txSession)
                    }.asList,
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
                    }.asSingle,
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
                    }.asList,
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
                    }.asList,
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
                            "sistEndret" to LocalDateTime.now().minusHours(1),
                        ),
                    ).map { row ->
                        // row.toInnsending(txSession)
                        row.toJournalpostId()
                    }.asList,
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
                    }.asSingle,
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
                            "sistEndret" to LocalDateTime.now().minusHours(1),
                        ),
                    ).map { row ->
                        row.toAntall()
                    }.asSingle,
                )
            }
        }!!
    }

    override fun lagre(innsending: Innsending): Innsending {
        val start = System.currentTimeMillis()
        val returnValue = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                if (innsendingFinnes(journalpostId = innsending.journalpostId, txSession = txSession)) {
                    val tid = System.currentTimeMillis()
                    val oppdatertInnsending = oppdater(innsending = innsending, txSession = txSession)
                    LOG.info { "Oppdater innsending tid tok ${System.currentTimeMillis() - tid} ms" }
                    oppdatertInnsending
                } else {
                    val tid = System.currentTimeMillis()
                    val lagretInnsending = insert(innsending = innsending, txSession = txSession)
                    LOG.info { "Insert innsending tid tok ${System.currentTimeMillis() - tid} ms" }
                    lagretInnsending
                }.also {
                    val tid1 = System.currentTimeMillis()
                    søknadDAO.lagre(innsendingId = innsending.id, søknad = innsending.søknad, txSession = txSession)
                    val tid2 = System.currentTimeMillis()
                    LOG.info { "søknadDAO.lagre tid tok ${tid2 - tid1} ms" }

                    tiltaksaktivitetDAO.lagre(
                        innsendingId = innsending.id,
                        tiltaksaktiviteter = innsending.tiltak?.tiltaksliste ?: emptyList(),
                        txSession = txSession,
                    )
                    val tid3 = System.currentTimeMillis()
                    LOG.info { "tiltaksaktivitetDAO.lagre tid tok ${tid3 - tid2} ms" }

                    ytelsesakDAO.lagre(
                        innsendingId = innsending.id,
                        ytelsesaker = innsending.ytelser?.ytelserliste ?: emptyList(),
                        txSession = txSession,
                    )
                    val tid4 = System.currentTimeMillis()
                    LOG.info { "ytelsesakDAO.lagre tid tok ${tid4 - tid3} ms" }

                    personopplysningerDAO.lagre(
                        innsendingId = innsending.id,
                        personopplysninger = innsending.personopplysninger?.personopplysningerliste ?: emptyList(),
                        txSession = txSession,
                    )
                    val tid5 = System.currentTimeMillis()
                    LOG.info { "personopplysningerDAO.lagre tid tok ${tid5 - tid4} ms" }

                    foreldrepengerVedtakDAO.lagre(
                        innsendingId = innsending.id,
                        foreldrepengerVedtak = innsending.foreldrepengerVedtak?.foreldrepengerVedtakliste
                            ?: emptyList(),
                        txSession = txSession,
                    )
                    val tid6 = System.currentTimeMillis()
                    LOG.info { "foreldrepengerVedtakDAO.lagre tid tok ${tid6 - tid5} ms" }

                    overgangsstønadVedtakDAO.lagre(
                        innsendingId = innsending.id,
                        overgangsstønadVedtak = innsending.overgangsstønadVedtak?.overgangsstønadVedtak
                            ?: emptyList(),
                        txSession = txSession,
                    )

                    uføreVedtakDAO.lagre(
                        innsendingId = innsending.id,
                        uføreVedtak = innsending.uføreVedtak?.uføreVedtak,
                        txSession = txSession,
                    )
                    val tid7 = System.currentTimeMillis()
                    LOG.info { "uføreVedtakDAO.lagre tid tok ${tid7 - tid6} ms" }

                    aktivitetsloggDAO.lagre(
                        innsendingId = innsending.id,
                        aktivitetslogg = innsending.aktivitetslogg,
                        txSession = txSession,
                    )
                    val tid8 = System.currentTimeMillis()
                    LOG.info { "aktivitetsloggDAO.lagre tid tok ${tid8 - tid7} ms" }
                }
            }
        }
        LOG.info { "PostgresInnsendingRepository.lagre tok ${(System.currentTimeMillis() - start)} ms" }
        return returnValue
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
            tidsstempelTiltakInnhentet = localDateTimeOrNull("tidsstempel_tiltak_innhentet"),
            tidsstempelPersonopplysningerInnhentet = localDateTimeOrNull("tidsstempel_personopplysninger_innhentet"),
            tidsstempelSkjermingInnhentet = localDateTimeOrNull("tidsstempel_skjerming_innhentet"),
            tidsstempelYtelserInnhentet = localDateTimeOrNull("tidsstempel_ytelser_innhentet"),
            tidsstempelForeldrepengerVedtakInnhentet = localDateTimeOrNull("tidsstempel_foreldrepengervedtak_innhentet"),
            tidsstempelOvergangsstønadVedtakInnhentet = localDateTimeOrNull("tidsstempel_overgangsstønadvedtak_innhentet"),
            tidsstempelUføreInnhentet = localDateTimeOrNull("tidsstempel_uførevedtak_innhentet"),
            tiltaksliste = tiltaksaktivitetDAO.hentForInnsending(id, txSession),
            ytelserliste = ytelsesakDAO.hentForInnsending(id, txSession),
            personopplysningerliste = personopplysningerDAO.hent(id, txSession),
            foreldrepengerVedtak = foreldrepengerVedtakDAO.hentForInnsending(id, txSession),
            overgangsstønadVedtak = overgangsstønadVedtakDAO.hentForInnsending(id, txSession),
            uføreVedtak = uføreVedtakDAO.hentForInnsending(id, txSession),
            aktivitetslogg = aktivitetsloggDAO.hent(id, txSession),
        )
    }

    private fun innsendingFinnes(journalpostId: String, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, journalpostId).map { row -> row.boolean("exists") }.asSingle,
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
                    "tidsstempel_tiltak_innhentet" to innsending.tiltak?.tidsstempelInnhentet,
                    "tidsstempel_personopplysninger_innhentet" to innsending.personopplysninger?.tidsstempelInnhentet,
                    "tidsstempel_skjerming_innhentet" to innsending.personopplysninger?.tidsstempelSkjermingInnhentet,
                    "tidsstempel_ytelser_innhentet" to innsending.ytelser?.tidsstempelInnhentet,
                    "tidsstempel_foreldrepengervedtak_innhentet" to innsending.foreldrepengerVedtak?.tidsstempelInnhentet,
                    "sist_endret" to innsending.sistEndret,
                    "opprettet" to nå,
                ),
            ).asUpdate,
        )
        return innsending
    }

    private fun oppdater(innsending: Innsending, txSession: TransactionalSession): Innsending {
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
                    "tidsstempel_tiltak_innhentet" to innsending.tiltak?.tidsstempelInnhentet,
                    "tidsstempel_personopplysninger_innhentet" to innsending.personopplysninger?.tidsstempelInnhentet,
                    "tidsstempel_skjerming_innhentet" to innsending.personopplysninger?.tidsstempelSkjermingInnhentet,
                    "tidsstempel_ytelser_innhentet" to innsending.ytelser?.tidsstempelInnhentet,
                    "tidsstempel_foreldrepengervedtak_innhentet" to innsending.foreldrepengerVedtak?.tidsstempelInnhentet,
                    "sistEndretOld" to sistEndretOld,
                    "sistEndret" to innsending.sistEndret,
                ),
            ).asUpdate,
        )
        if (antRaderOppdatert == 0) {
            throw IllegalStateException("Noen andre har endret denne")
        }
        return innsending
    }

    @Language("SQL")
    private val lagre = """
        insert into innsending (
            id, 
            journalpost_id, 
            ident, 
            tilstand, 
            tidsstempel_tiltak_innhentet, 
            tidsstempel_personopplysninger_innhentet, 
            tidsstempel_skjerming_innhentet, 
            tidsstempel_ytelser_innhentet, 
            tidsstempel_foreldrepengervedtak_innhentet, 
            sist_endret, 
            opprettet
        ) values (
            :id, 
            :journalpostId, 
            :ident, 
            :tilstand, 
            :tidsstempel_tiltak_innhentet, 
            :tidsstempel_personopplysninger_innhentet, 
            :tidsstempel_skjerming_innhentet, 
            :tidsstempel_ytelser_innhentet, 
            :tidsstempel_foreldrepengervedtak_innhentet, 
            :sist_endret, 
            :opprettet
        )
    """.trimIndent()

    @Language("SQL")
    private val oppdater =
        """update innsending set 
              tilstand = :tilstand,
              sist_endret = :sistEndret,
              tidsstempel_tiltak_innhentet = :tidsstempel_tiltak_innhentet,
              tidsstempel_personopplysninger_innhentet = :tidsstempel_personopplysninger_innhentet,
              tidsstempel_skjerming_innhentet = :tidsstempel_skjerming_innhentet,
              tidsstempel_ytelser_innhentet = :tidsstempel_ytelser_innhentet,
              tidsstempel_foreldrepengervedtak_innhentet = :tidsstempel_foreldrepengervedtak_innhentet
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
