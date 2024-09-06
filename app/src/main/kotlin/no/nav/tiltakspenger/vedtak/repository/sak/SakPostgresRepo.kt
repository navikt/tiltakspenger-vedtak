package no.nav.tiltakspenger.vedtak.repository.sak

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionContext.Companion.withSession
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.meldekort.domene.Meldeperioder
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.sak.TynnSak
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.vedtak.RammevedtakPostgresRepo
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.LocalDateTime

internal class SakPostgresRepo(
    private val sessionFactory: PostgresSessionFactory,
) : SakRepo {

    override fun hentForFnr(fnr: Fnr): Saker {
        val saker =
            sessionFactory.withSessionContext { sessionContext ->
                sessionContext.withSession { session ->
                    session.run(
                        queryOf(
                            sqlHentSakerForIdent,
                            mapOf("ident" to fnr.verdi),
                        ).map { row ->
                            row.toSak(sessionContext)
                        }.asList,
                    )
                }
            }
        return Saker(
            fnr = fnr,
            saker = saker,
        )
    }

    override fun hentForSaksnummer(saksnummer: Saksnummer): Sak? =
        sessionFactory.withSessionContext { sessionContext ->
            sessionContext.withSession { session ->
                session.run(
                    queryOf(
                        sqlHentSakForSaksnummer,
                        mapOf("saksnummer" to saksnummer.toString()),
                    ).map { row ->
                        row.toSak(sessionContext)
                    }.asSingle,
                )
            }
        }

    override fun hentForSakId(sakId: SakId): Sak? {
        return sessionFactory.withSessionContext { sessionContext ->
            sessionContext.withSession { session ->
                session.run(
                    queryOf(
                        sqlHent,
                        mapOf("id" to sakId.toString()),
                    ).map { row ->
                        row.toSak(sessionContext)
                    }.asSingle,
                )
            }
        }
    }

    override fun hentDetaljerForSakId(sakId: SakId): TynnSak? {
        return sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    sqlHent,
                    mapOf("id" to sakId.toString()),
                ).map { row ->
                    row.toSakDetaljer()
                }.asSingle,
            )
        }
    }

    override fun hentFnrForSakId(
        sakId: SakId,
        sessionContext: SessionContext?,
    ): Fnr? {
        return sessionFactory.withSession(sessionContext) { session ->
            session.run(
                queryOf(
                    "select ident as fnr from sak  where sak.id = :sakId",
                    mapOf("sakId" to sakId.toString()),
                ).map { row ->
                    Fnr.fromString(row.string("fnr"))
                }.asSingle,
            )
        }
    }

    override fun lagre(
        sak: Sak,
        transactionContext: TransactionContext?,
    ): Sak =
        sessionFactory.withTransaction(transactionContext) { txSession ->
            val sistEndret = hentSistEndret(sak.id, txSession)
            val opprettetSak =
                if (sistEndret == null) {
                    opprettSak(sak, txSession)
                } else {
                    oppdaterSak(sistEndret, sak, txSession)
                }
            PersonopplysningerPostgresRepo.lagre(
                sakId = sak.id,
                personopplysninger = sak.personopplysninger,
                txSession = txSession,
            )
            sak.behandlinger.filterIsInstance<Førstegangsbehandling>().forEach {
                BehandlingPostgresRepo.lagre(it, txSession)
            }
            opprettetSak
        }

    override fun hentNesteSaksnummer(): Saksnummer {
        val iDag = LocalDate.now()
        val saksnummerPrefiks = Saksnummer.genererSaksnummerPrefiks(iDag)
        return sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    sqlHentNesteLøpenummer,
                    mapOf("saksnummerprefiks" to "$saksnummerPrefiks%"),
                ).map { row ->
                    row
                        .string("saksnummer")
                        .let { Saksnummer(it).nesteSaksnummer() }
                }.asSingle,
            )
        } ?: Saksnummer.genererSaknummer(dato = iDag)
    }

    override fun hentForFørstegangsbehandlingId(behandlingId: BehandlingId): Sak? {
        return sessionFactory.withSessionContext { sessionContext ->
            sessionContext.withSession { session ->
                session.run(
                    queryOf(
                        "select * from sak where id = (select sakid from behandling where id = :behandlingId)",
                        mapOf("behandlingId" to behandlingId.toString()),
                    ).map { row ->
                        row.toSak(sessionContext)
                    }.asSingle,
                )
            }
        }
    }

    override fun hentForSøknadId(søknadId: SøknadId): Sak? {
        return sessionFactory.withSessionContext { sessionContext ->
            sessionContext.withSession { session ->
                session.run(
                    queryOf(
                        """
                           select s.* from søknad sø
                           left join behandling b on b.id = sø.behandling_id
                           left join sak s on s.id = b.sakid
                           where sø.id = :soknadId
                        """.trimIndent(),
                        mapOf("soknadId" to søknadId.toString()),
                    ).map { row ->
                        row.toSak(sessionContext)
                    }.asSingle,
                )
            }
        }
    }

    companion object {
        private val SECURELOG = KotlinLogging.logger("tjenestekall")

        private fun hentSistEndret(
            sakId: SakId,
            session: Session,
        ): LocalDateTime? =
            session.run(
                queryOf(
                    sqlHentSistEndret,
                    mapOf("id" to sakId.toString()),
                ).map { row -> row.localDateTime("sist_endret") }.asSingle,
            )

        private fun oppdaterSak(
            sistEndret: LocalDateTime,
            sak: Sak,
            session: Session,
        ): Sak {
            SECURELOG.info { "Oppdaterer sak ${sak.id}" }

            val antRaderOppdatert =
                session.run(
                    queryOf(
                        sqlOppdaterSak,
                        mapOf(
                            "id" to sak.id.toString(),
                            "ident" to sak.fnr.verdi,
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

        private fun opprettSak(
            sak: Sak,
            session: Session,
        ): Sak {
            SECURELOG.info { "Oppretter sak ${sak.id}" }

            val nå = nå()

            session.run(
                queryOf(
                    sqlOpprettSak,
                    mapOf(
                        "id" to sak.id.toString(),
                        "ident" to sak.fnr.verdi,
                        "saksnummer" to sak.saksnummer.verdi,
                        "sistEndret" to nå,
                        "opprettet" to nå,
                    ),
                ).asUpdate,
            )
            return sak
        }

        private fun Row.toSak(sessionContext: SessionContext): Sak {
            val id = SakId.fromString(string("id"))
            return sessionContext.withSession { session ->
                val behandlinger = BehandlingPostgresRepo.hentForSakId(id, session)
                Sak(
                    sakDetaljer = toSakDetaljer(),
                    behandlinger = behandlinger,
                    personopplysninger = PersonopplysningerPostgresRepo.hentForSakId(id, session),
                    vedtak = RammevedtakPostgresRepo.hentForSakId(id, sessionContext),
                    meldekort = MeldekortPostgresRepo.hentForSakId(id, session) ?: Meldeperioder.empty(behandlinger.first().tiltakstype),
                )
            }
        }

        private fun Row.toSakDetaljer(): TynnSak {
            val id = SakId.fromString(string("id"))
            return TynnSak(
                id = id,
                fnr = Fnr.fromString(string("ident")),
                saksnummer = Saksnummer(verdi = string("saksnummer")),
            )
        }

        @Language("SQL")
        private val sqlOpprettSak =
            """
        insert into sak (
            id,
            ident,
            saksnummer,
            sist_endret,
            opprettet
        ) values (
            :id,
            :ident,
            :saksnummer,
            :sistEndret,
            :opprettet
        )
            """.trimIndent()

        @Language("SQL")
        private val sqlOppdaterSak =
            """update sak set 
              ident = :ident,
              sist_endret = :sistEndret
           where id = :id
             and sist_endret = :sistEndretOld
            """.trimMargin()

        @Language("SQL")
        private val sqlHent =
            """select * from sak where id = :id""".trimIndent()

        @Language("SQL")
        private val sqlHentForJournalpost =
            """
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
        private val sqlHentSakForSaksnummer =
            """select * from sak where saksnummer = :saksnummer""".trimIndent()

        @Language("SQL")
        private val sqlHentSistEndret =
            """select sist_endret from sak where id = :id""".trimIndent()

        @Language("SQL")
        private val sqlHentNesteLøpenummer =
            """
        SELECT saksnummer 
        FROM sak 
        WHERE saksnummer LIKE :saksnummerprefiks 
        ORDER BY saksnummer DESC 
        LIMIT 1
            """.trimIndent()
    }
}
