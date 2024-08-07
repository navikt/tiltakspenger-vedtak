package no.nav.tiltakspenger.vedtak.repository.behandling

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import kotliquery.Row
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toVilkårssett
import no.nav.tiltakspenger.vedtak.repository.behandling.stønadsdager.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.stønadsdager.toStønadsdager
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PostgresBehandlingRepo(
    private val søknadDAO: SøknadDAO,
    private val sessionFactory: PostgresSessionFactory,
) : BehandlingRepo,
    BehandlingDAO {
    override fun hentOrNull(
        behandlingId: BehandlingId,
        sessionContext: SessionContext?,
    ): Førstegangsbehandling? =
        sessionFactory.withSession(sessionContext) { session ->
            session.run(
                queryOf(
                    sqlHentBehandling,
                    mapOf(
                        "id" to behandlingId.toString(),
                    ),
                ).map { row ->
                    row.toBehandling(session)
                }.asSingle,
            )
        }

    override fun hent(
        behandlingId: BehandlingId,
        sessionContext: SessionContext?,
    ): Førstegangsbehandling =
        hentOrNull(behandlingId, sessionContext)
            ?: throw IkkeFunnetException("Behandling med id $behandlingId ikke funnet")

    override fun hentAlleForIdent(fnr: Fnr): List<Førstegangsbehandling> =
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    sqlHentBehandlingForIdent,
                    mapOf(
                        "ident" to fnr.verdi,
                    ),
                ).map { row ->
                    row.toBehandling(session)
                }.asList,
            )
        }

    override fun hentForSak(
        sakId: SakId,
        session: Session,
    ): NonEmptyList<Førstegangsbehandling> =
        session
            .run(
                queryOf(
                    sqlHentBehandlingForSak,
                    mapOf(
                        "sakId" to sakId.toString(),
                    ),
                ).map { row ->
                    row.toBehandling(session)
                }.asList,
            ).toNonEmptyListOrNull()
            ?: throw IkkeFunnetException("sak med id $sakId ikke funnet")

    override fun hentForSak(sakId: SakId): NonEmptyList<Førstegangsbehandling> =
        sessionFactory.withSession { session ->
            hentForSak(sakId, session)
        }

    override fun hentForJournalpostId(journalpostId: String): Førstegangsbehandling? =
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    sqlHentBehandlingForJournalpostId,
                    mapOf(
                        "journalpostId" to journalpostId,
                    ),
                ).map { row ->
                    row.toBehandling(session)
                }.asSingle,
            )
        }

    override fun hentForSøknadId(søknadId: SøknadId): Førstegangsbehandling? =
        sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    """
                    select b.*,sak.saksnummer,sak.ident
                      from behandling b
                      join søknad s on b.id = s.behandling_id
                      join sak on sak.id = b.sakId
                      where s.id = :id
                    """.trimIndent(),
                    mapOf(
                        "id" to søknadId.toString(),
                    ),
                ).map { row ->
                    row.toBehandling(session)
                }.asSingle,
            )
        }

    override fun lagre(
        behandling: Behandling,
        transactionContext: TransactionContext?,
    ) = sessionFactory.withTransaction(transactionContext) { tx ->
        lagre(behandling, tx)
    }

    /**
     * Vil ikke overlagre søknad, men kun knytte søknadene til behandlingen.
     */
    override fun lagre(
        behandling: Behandling,
        tx: TransactionalSession,
    ) {
        val sistEndret = hentSistEndret(behandling.id, tx)
        if (sistEndret == null) {
            opprettBehandling(behandling, tx)
        } else {
            oppdaterBehandling(sistEndret, behandling, tx)
        }.also {
            if (behandling is Førstegangsbehandling) {
                søknadDAO.knyttSøknadTilBehandling(behandling.id, behandling.søknad.id, tx)
            }
        }
    }

    private fun oppdaterBehandling(
        sistEndret: LocalDateTime,
        behandling: Behandling,
        session: Session,
    ) {
        SECURELOG.info { "Oppdaterer behandling ${behandling.id}" }

        val antRaderOppdatert =
            session.run(
                queryOf(
                    sqlOppdaterBehandling,
                    mapOf(
                        "id" to behandling.id.toString(),
                        "sakId" to behandling.sakId.toString(),
                        "fom" to behandling.vurderingsperiode.fraOgMed,
                        "tom" to behandling.vurderingsperiode.tilOgMed,
                        "status" to behandling.status.toDb(),
                        "sistEndretOld" to sistEndret,
                        "sistEndret" to nå(),
                        "saksbehandler" to behandling.saksbehandler,
                        "beslutter" to behandling.beslutter,
                        "vilkaarssett" to behandling.vilkårssett.toDbJson(),
                        "stonadsdager" to behandling.stønadsdager.toDbJson(),
                    ),
                ).asUpdate,
            )
        if (antRaderOppdatert == 0) {
            throw IllegalStateException("Noen andre har endret denne behandlingen ${behandling.id}")
        }
    }

    private fun opprettBehandling(
        behandling: Behandling,
        session: Session,
    ) {
        SECURELOG.info { "Oppretter behandling ${behandling.id}" }

        val nå = nå()

        session.run(
            queryOf(
                sqlOpprettBehandling,
                mapOf(
                    "id" to behandling.id.toString(),
                    "sakId" to behandling.sakId.toString(),
                    "fom" to behandling.vurderingsperiode.fraOgMed,
                    "tom" to behandling.vurderingsperiode.tilOgMed,
                    "status" to behandling.status.toDb(),
                    "sistEndret" to nå,
                    "opprettet" to nå,
                    "vilkaarssett" to behandling.vilkårssett.toDbJson(),
                    "stonadsdager" to behandling.stønadsdager.toDbJson(),
                    "saksbehandler" to behandling.saksbehandler,
                    "beslutter" to behandling.beslutter,
                ),
            ).asUpdate,
        )
    }

    private fun hentSistEndret(
        behandlingId: BehandlingId,
        session: Session,
    ): LocalDateTime? =
        session.run(
            queryOf(
                sqlHentSistEndret,
                mapOf(
                    "id" to behandlingId.toString(),
                ),
            ).map { row -> row.localDateTime("sist_endret") }.asSingle,
        )

    private fun Row.toBehandling(session: Session): Førstegangsbehandling {
        val id = BehandlingId.fromString(string("id"))
        val sakId = SakId.fromString(string("sakId"))
        val vurderingsperiode = Periode(localDate("fom"), localDate("tom"))
        val status = string("status")
        val saksbehandler = stringOrNull("saksbehandler")
        val beslutter = stringOrNull("beslutter")
        val søknad = søknadDAO.hentForBehandlingId(id, session)!!

        val stønadsdager = string("stønadsdager").toStønadsdager()
        val vilkårssett = string("vilkårssett").toVilkårssett(vurderingsperiode)
        val fnr = Fnr.fromString(string("ident"))
        val saksnummer = Saksnummer(string("saksnummer"))

        return Førstegangsbehandling(
            id = id,
            sakId = sakId,
            saksnummer = saksnummer,
            fnr = fnr,
            søknad = søknad,
            vurderingsperiode = vurderingsperiode,
            vilkårssett = vilkårssett,
            saksbehandler = saksbehandler,
            beslutter = beslutter,
            stønadsdager = stønadsdager,
            status = status.toBehandlingsstatus(),
        )
    }

    private val sqlHentSistEndret =
        """
        select sist_endret from behandling where id = :id
        """.trimIndent()

    @Language("SQL")
    private val sqlOpprettBehandling =
        """
        insert into behandling (
            id,
            sakId,
            fom,
            tom,
            status,
            sist_endret,
            opprettet,
            vilkårssett,
            stønadsdager,
            saksbehandler,
            beslutter
        ) values (
            :id,
            :sakId,
            :fom,
            :tom,
            :status,
            :sistEndret,
            :opprettet,
            to_jsonb(:vilkaarssett::jsonb),
            to_jsonb(:stonadsdager::jsonb),
            :saksbehandler,
            :beslutter
        )
        """.trimIndent()

    @Language("SQL")
    private val sqlOppdaterBehandling =
        """
        update behandling set 
            fom = :fom,
            tom = :tom,
            sakId = :sakId,
            status = :status,
            sist_endret = :sistEndret,
            saksbehandler = :saksbehandler,
            beslutter = :beslutter,
            vilkårssett = to_jsonb(:vilkaarssett::json),
            stønadsdager = to_jsonb(:stonadsdager::json)
        where id = :id
          and sist_endret = :sistEndretOld
        """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandling =
        """
        select b.*,s.ident, s.saksnummer from behandling b join sak s on s.id = b.sakid where b.id = :id
        """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandlingForSak =
        """
        select b.*,s.ident, s.saksnummer from behandling b join sak s on s.id = b.sakid where b.sakId = :sakId
        """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandlingForIdent =
        """
        select b.*,s.ident, s.saksnummer from behandling b
          join sak s on s.id = b.sakid
           where s.ident = :ident
        """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandlingForJournalpostId =
        """
         select b.*,s.ident, s.saksnummer from behandling b
         join sak s on s.id = b.sakid
        where b.id = 
           (select behandling_id 
            from søknad 
            where journalpost_id = :journalpostId)
        """.trimIndent()

    @Language("SQL")
    private val sqlHentAlleBehandlinger =
        """
        select b.*,s.ident, s.saksnummer from behandling b
         join sak s on s.id = b.sakid
        """.trimIndent()
}
