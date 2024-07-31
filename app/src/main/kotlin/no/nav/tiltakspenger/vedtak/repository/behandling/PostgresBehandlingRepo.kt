package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toVilkårssett
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

// todo Må enten endres til å kunne hente og lagre alle typer behandlinger og ikke bare Søknadsbehandlinger
//      eller så må vi lage egne Repo for de andre type behandlingene
internal class PostgresBehandlingRepo(
    private val vurderingRepo: VurderingRepo,
    private val søknadDAO: SøknadDAO,
    private val utfallsperiodeDAO: UtfallsperiodeDAO,
    private val sessionFactory: PostgresSessionFactory,
) : BehandlingRepo, BehandlingDAO {
    override fun hentOrNull(behandlingId: BehandlingId, sessionContext: SessionContext?): Førstegangsbehandling? {
        return sessionFactory.withSession(sessionContext) { session ->
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
    }

    override fun hent(behandlingId: BehandlingId, sessionContext: SessionContext?): Førstegangsbehandling {
        return hentOrNull(behandlingId, sessionContext)
            ?: throw IkkeFunnetException("Behandling med id $behandlingId ikke funnet")
    }

    /**
     * TODO jah: Denne kan potensielt hente veldig mye data, bør kun hente akkurat det vi trenger i frontend.
     */
    override fun hentAlle(): List<Førstegangsbehandling> {
        return sessionFactory.withSession { session ->
            session.run(
                queryOf(
                    sqlHentAlleBehandlinger,
                ).map { row ->
                    row.toBehandling(session)
                }.asList,
            )
        }
    }

    override fun hentAlleForIdent(ident: String): List<Førstegangsbehandling> {
        return sessionFactory.withTransaction { txSession ->
            txSession.run(
                queryOf(
                    sqlHentBehandlingForIdent,
                    mapOf(
                        "ident" to ident,
                    ),
                ).map { row ->
                    row.toBehandling(txSession)
                }.asList,
            )
        }
    }

    override fun hentForSak(sakId: SakId, session: Session): List<Førstegangsbehandling> {
        return session.run(
            queryOf(
                sqlHentBehandlingForSak,
                mapOf(
                    "sakId" to sakId.toString(),
                ),
            ).map { row ->
                row.toBehandling(session)
            }.asList,
        )
    }

    override fun hentForSak(sakId: SakId): List<Førstegangsbehandling> {
        return sessionFactory.withSession { session ->
            hentForSak(sakId, session)
        }
    }

    override fun hentForJournalpostId(journalpostId: String): Førstegangsbehandling? {
        return sessionFactory.withTransaction { txSession ->
            txSession.run(
                queryOf(
                    sqlHentBehandlingForJournalpostId,
                    mapOf(
                        "journalpostId" to journalpostId,
                    ),
                ).map { row ->
                    row.toBehandling(txSession)
                }.asSingle,
            )
        }
    }

    override fun hentForSøknadId(søknadId: SøknadId): Førstegangsbehandling? {
        return sessionFactory.withTransaction { txSession ->
            txSession.run(
                queryOf(
                    "select * from behandling b join søknad s on b.id = s.behandling_id where s.id = :id",
                    mapOf(
                        "id" to søknadId.toString(),
                    ),
                ).map { row ->
                    row.toBehandling(txSession)
                }.asSingle,
            )
        }
    }

    override fun lagre(behandling: Behandling, transactionContext: TransactionContext?) {
        return sessionFactory.withTransaction(transactionContext) { tx ->
            lagre(behandling, tx)
        }
    }

    /**
     * Vil ikke overlagre søknad, men kun knytte søknadene til behandlingen.
     */
    override fun lagre(behandling: Behandling, tx: TransactionalSession) {
        val sistEndret = hentSistEndret(behandling.id, tx)
        if (sistEndret == null) {
            opprettBehandling(behandling, tx)
        } else {
            oppdaterBehandling(sistEndret, behandling, tx)
        }.also {
            if (behandling is Førstegangsbehandling) {
                søknadDAO.knyttSøknaderTilBehandling(behandling.id, behandling.søknader.map { it.id }, tx)
            }
            vurderingRepo.lagre(behandling.id, behandling.vilkårsvurderinger, tx)
        }
    }

    private fun oppdaterBehandling(
        sistEndret: LocalDateTime,
        behandling: Behandling,
        txSession: TransactionalSession,
    ) {
        SECURELOG.info { "Oppdaterer behandling ${behandling.id}" }

        val antRaderOppdatert = txSession.run(
            queryOf(
                sqlOppdaterBehandling,
                mapOf(
                    "id" to behandling.id.toString(),
                    "sakId" to behandling.sakId.toString(),
                    "fom" to behandling.vurderingsperiode.fraOgMed,
                    "tom" to behandling.vurderingsperiode.tilOgMed,
                    "tilstand" to finnTilstand(behandling),
                    "status" to finnStatus(behandling),
                    "sistEndretOld" to sistEndret,
                    "sistEndret" to nå(),
                    "saksbehandler" to behandling.saksbehandler,
                    "beslutter" to behandling.beslutter,
                    "vilkaarssett" to behandling.vilkårssett.toDbJson(),
                ),
            ).asUpdate,
        )
        if (antRaderOppdatert == 0) {
            throw IllegalStateException("Noen andre har endret denne behandlingen ${behandling.id}")
        }
    }

    private fun opprettBehandling(
        behandling: Behandling,
        txSession: TransactionalSession,
    ) {
        SECURELOG.info { "Oppretter behandling ${behandling.id}" }

        val nå = nå()

        txSession.run(
            queryOf(
                sqlOpprettBehandling,
                mapOf(
                    "id" to behandling.id.toString(),
                    "sakId" to behandling.sakId.toString(),
                    "fom" to behandling.vurderingsperiode.fraOgMed,
                    "tom" to behandling.vurderingsperiode.tilOgMed,
                    "tilstand" to finnTilstand(behandling),
                    "status" to finnStatus(behandling),
                    "sistEndret" to nå,
                    "opprettet" to nå,
                    "vilkaarssett" to behandling.vilkårssett.toDbJson(),
                ),
            ).asUpdate,
        )
    }

    private fun hentSistEndret(behandlingId: BehandlingId, txSession: TransactionalSession): LocalDateTime? =
        txSession.run(
            queryOf(
                sqlHentSistEndret,
                mapOf(
                    "id" to behandlingId.toString(),
                ),
            ).map { row -> row.localDateTime("sist_endret") }.asSingle,
        )

    private fun Row.toBehandling(session: Session): Førstegangsbehandling {
        val id = BehandlingId.fromString(string("id"))
        val sakId = SakId.fromDb(string("sakId"))
        val fom = localDate("fom")
        val tom = localDate("tom")
        val status = string("status")
        val saksbehandler = stringOrNull("saksbehandler")
        val beslutter = stringOrNull("beslutter")
        val behandlingStatus = when (status) {
            "Innvilget" -> BehandlingStatus.Innvilget
            "Avslag" -> BehandlingStatus.Avslag
            "Manuell" -> BehandlingStatus.Manuell
            else -> throw IllegalStateException("Ukjent BehandlingVilkårsvurdert $id med status $status")
        }
        val tilstand = when (val type = string("tilstand")) {
            "søknadsbehandling" -> BehandlingTilstand.OPPRETTET
            "UnderBehandling" -> BehandlingTilstand.UNDER_BEHANDLING
            "TilBeslutting" -> BehandlingTilstand.TIL_BESLUTTER
            "Iverksatt" -> BehandlingTilstand.IVERKSATT
            else -> throw IllegalStateException("Hentet en Behandling $id med ukjent status : $type")
        }
        val søknader = søknadDAO.hentForBehandlingId(id, session)

        val vilkårssett = string("vilkårssett").toVilkårssett(
            vilkårsvurderinger = vurderingRepo.hent(id, session),
            utfallsperioder = utfallsperiodeDAO.hent(id, session),
        )
        val ident = string("ident")
        val saksnummer = Saksnummer(string("saksnummer"))
        return Førstegangsbehandling(
            id = id,
            sakId = sakId,
            saksnummer = saksnummer,
            ident = ident,
            søknader = søknader,
            vurderingsperiode = Periode(fom, tom),
            vilkårssett = vilkårssett,
            saksbehandler = saksbehandler,
            beslutter = beslutter,
            status = behandlingStatus,
            tilstand = tilstand,
        )
    }

    private fun finnTilstand(behandling: Behandling): String =
        when (behandling.tilstand) {
            BehandlingTilstand.OPPRETTET -> "søknadsbehandling"
            BehandlingTilstand.UNDER_BEHANDLING -> "UnderBehandling"
            BehandlingTilstand.TIL_BESLUTTER -> "TilBeslutting"
            BehandlingTilstand.IVERKSATT -> "Iverksatt"
        }

    private fun finnStatus(behandling: Behandling): String =
        when (behandling.status) {
            BehandlingStatus.Innvilget -> "Innvilget"
            BehandlingStatus.Manuell -> "Manuell"
            BehandlingStatus.Avslag -> "Avslag"
        }

    private val sqlHentSistEndret = """
        select sist_endret from behandling where id = :id
    """.trimIndent()

    @Language("SQL")
    private val sqlOpprettBehandling = """
        insert into behandling (
            id,
            sakId,
            fom,
            tom,
            tilstand,
            status,
            sist_endret,
            opprettet,
            vilkårssett
        ) values (
            :id,
            :sakId,
            :fom,
            :tom,
            :tilstand,
            :status,
            :sistEndret,
            :opprettet,
            to_jsonb(:vilkaarssett::jsonb)
        )
    """.trimIndent()

    @Language("SQL")
    private val sqlOppdaterBehandling = """
        update behandling set 
            fom = :fom,
            tom = :tom,
            sakId = :sakId,
            tilstand = :tilstand,
            status = :status,
            sist_endret = :sistEndret,
            saksbehandler = :saksbehandler,
            beslutter = :beslutter,
            vilkårssett = to_jsonb(:vilkaarssett::json)
        where id = :id
          and sist_endret = :sistEndretOld
    """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandling = """
        select b.*,s.ident, s.saksnummer from behandling b join sak s on s.id = b.sakid where b.id = :id
    """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandlingForSak = """
         select b.*,s.ident, s.saksnummer from behandling b join sak s on s.id = b.sakid where b.sakId = :sakId
    """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandlingForIdent = """
        select b.*,s.ident, s.saksnummer from behandling b
          join sak s on s.id = b.sakid
           where s.ident = :ident
    """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandlingForJournalpostId = """
          select b.*,s.ident, s.saksnummer from behandling b
          join sak s on s.id = b.sakid
         where b.id = 
            (select behandling_id 
             from søknad 
             where journalpost_id = :journalpostId)
    """.trimIndent()

    @Language("SQL")
    private val sqlHentAlleBehandlinger = """
         select b.*,s.ident, s.saksnummer from behandling b
          join sak s on s.id = b.sakid
    """.trimIndent()
}
