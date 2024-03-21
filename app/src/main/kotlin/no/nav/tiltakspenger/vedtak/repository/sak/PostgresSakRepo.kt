package no.nav.tiltakspenger.vedtak.repository.sak

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.sak.TynnSak
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakRepoImpl
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PostgresSakRepo(
    private val behandlingRepo: BehandlingRepo = PostgresBehandlingRepo(),
    private val personopplysningerRepo: PostgresPersonopplysningerRepo = PostgresPersonopplysningerRepo(),
    private val vedtakRepo: VedtakRepo = VedtakRepoImpl(),
) : SakRepo {
    override fun hentForIdentMedPeriode(fnr: String, periode: Periode): List<Sak> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentSakerForIdent,
                        mapOf("ident" to fnr),
                    ).map { row ->
                        row.toSak(txSession)
                    }.asList,
                )
            }
        }.filter {
            it.periode.overlapperMed(periode)
        }
    }

    override fun hentForJournalpostId(journalpostId: String): Sak? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentForJournalpost,
                        mapOf(
                            "journalpostId" to journalpostId,
                        ),
                    ).map { row ->
                        row.toSak(txSession)
                    }.asSingle,
                )
            }
        }
    }

    override fun hent(sakId: SakId): Sak? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHent,
                        mapOf("id" to sakId.toString()),
                    ).map { row ->
                        row.toSak(txSession)
                    }.asSingle,
                )
            }
        }
    }

    override fun hentSakDetaljer(sakId: SakId): TynnSak? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHent,
                        mapOf("id" to sakId.toString()),
                    ).map { row ->
                        row.toSakDetaljer()
                    }.asSingle,
                )
            }
        }
    }

    override fun lagre(sak: Sak): Sak {
        return sessionOf(DataSource.hikariDataSource).use { session ->
            session.transaction { txSession ->
                val sistEndret = hentSistEndret(sak.id, txSession)
                val opprettetSak = if (sistEndret == null) {
                    opprettSak(sak, txSession)
                } else {
                    oppdaterSak(sistEndret, sak, txSession)
                }
                personopplysningerRepo.lagre(
                    sakId = sak.id,
                    personopplysninger = sak.personopplysninger,
                    txSession = txSession,
                )
                opprettetSak
            }.also { sak ->
                sak.behandlinger.filterIsInstance<Førstegangsbehandling>().forEach {
                    behandlingRepo.lagre(it)
                }
            }
        }
    }

    private fun hentSistEndret(sakId: SakId, txSession: TransactionalSession): LocalDateTime? = txSession.run(
        queryOf(
            sqlHentSistEndret,
            mapOf("id" to sakId.toString()),
        ).map { row -> row.localDateTime("sist_endret") }.asSingle,
    )

    private fun oppdaterSak(
        sistEndret: LocalDateTime,
        sak: Sak,
        txSession: TransactionalSession,
    ): Sak {
        SECURELOG.info { "Oppdaterer sak ${sak.id}" }

        val antRaderOppdatert = txSession.run(
            queryOf(
                sqlOppdaterSak,
                mapOf(
                    "id" to sak.id.toString(),
                    "fom" to sak.periode.fra,
                    "tom" to sak.periode.til,
                    "ident" to sak.ident,
                    "sistEndretOld" to sistEndret,
                    "sistEndret" to nå(),
                ),
            ).asUpdate,
        )
        if (antRaderOppdatert == 0) {
            throw IllegalStateException("Noen andre har endret denne saken ${sak.id}")
        }
        return sak
    }

    private fun opprettSak(sak: Sak, txSession: TransactionalSession): Sak {
        SECURELOG.info { "Oppretter sak ${sak.id}" }

        val nå = nå()

        txSession.run(
            queryOf(
                sqlOpprettSak,
                mapOf(
                    "id" to sak.id.toString(),
                    "ident" to sak.ident,
                    "saksnummer" to sak.saknummer.verdi,
                    "fom" to sak.periode.fra,
                    "tom" to sak.periode.til,
                    "sistEndret" to nå,
                    "opprettet" to nå,
                ),
            ).asUpdate,
        )
        return sak
    }

    override fun hentNesteLøpenr(): String {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentNesteLøpenummer,
                    ).map { row ->
                        row.int("løpenr").toString()
                    }.asSingle,
                )
            }
        } ?: throw IllegalStateException("Kunne ikke hente neste løpenummer")
    }

    override fun resetLøpenummer() {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlResetLøpenummer,
                    ).asExecute,
                )
            }
        }
    }

    private fun Row.toSak(txSession: TransactionalSession): Sak {
        val id = SakId.fromDb(string("id"))
        return Sak(
            sakDetaljer = toSakDetaljer(),
            behandlinger = behandlingRepo.hentForSak(id),
            personopplysninger = personopplysningerRepo.hent(id, txSession),
            vedtak = vedtakRepo.hentVedtakForSak(id),
        )
    }

    private fun Row.toSakDetaljer(): TynnSak {
        val id = SakId.fromDb(string("id"))
        return TynnSak(
            id = id,
            ident = string("ident"),
            saknummer = Saksnummer(verdi = string("saksnummer")),
            periode = Periode(fra = localDate("fom"), til = localDate("tom")),
        )
    }

    @Language("SQL")
    private val sqlOpprettSak = """
        insert into sak (
            id,
            ident,
            saksnummer,
            fom,
            tom,
            sist_endret,
            opprettet
        ) values (
            :id,
            :ident,
            :saksnummer,
            :fom,
            :tom,
            :sistEndret,
            :opprettet
        )
    """.trimIndent()

    @Language("SQL")
    private val sqlOppdaterSak =
        """update sak set 
              ident = :ident,
              fom   = :fom,
              tom   = :tom,
              sist_endret = :sistEndret
           where id = :id
             and sist_endret = :sistEndretOld
        """.trimMargin()

    @Language("SQL")
    private val sqlHent =
        """select * from sak where id = :id""".trimIndent()

    @Language("SQL")
    private val sqlHentForJournalpost = """
        select * 
          from sak 
         where id = (select sakid 
                       from behandling 
                      where id = (select behandling_id
                                    from søknad
                                   where journalpost_id = :journalpostId
                                    )
                       )
    """.trimIndent()

    @Language("SQL")
    private val sqlHentSakerForIdent =
        """select * from sak where ident = :ident""".trimIndent()

    @Language("SQL")
    private val sqlHentSistEndret =
        """select sist_endret from sak where id = :id""".trimIndent()

    @Language("SQL")
    private val sqlHentNesteLøpenummer =
        """select nextval('sak_løpenr') as løpenr""".trimIndent()

    @Language("SQL")
    private val sqlResetLøpenummer =
        """alter sequence sak_løpenr restart""".trimIndent()
}
