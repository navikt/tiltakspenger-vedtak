package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.TiltakVilkår
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
    private val saksopplysningRepo: SaksopplysningRepo,
    private val vurderingRepo: VurderingRepo,
    private val søknadDAO: SøknadDAO,
    private val tiltakDAO: TiltakDAO,
    private val utfallsperiodeDAO: UtfallsperiodeDAO,
    private val sessionFactory: PostgresSessionFactory,
) : BehandlingRepo, BehandlingDAO {
    override fun hentOrNull(behandlingId: BehandlingId): Førstegangsbehandling? {
        return sessionFactory.withTransaction { txSession ->
            txSession.run(
                queryOf(
                    sqlHentBehandling,
                    mapOf(
                        "id" to behandlingId.toString(),
                    ),
                ).map { row ->
                    row.toBehandling(txSession)
                }.asSingle,
            )
        }
    }

    override fun hent(behandlingId: BehandlingId): Førstegangsbehandling {
        return hentOrNull(behandlingId) ?: throw IkkeFunnetException("Behandling med id $behandlingId ikke funnet")
    }

    override fun hentAlle(): List<Førstegangsbehandling> {
        return sessionFactory.withTransaction { txSession ->
            txSession.run(
                queryOf(
                    sqlHentAlleBehandlinger,
                ).map { row ->
                    row.toBehandling(txSession)
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

    override fun hentForSak(sakId: SakId, tx: TransactionalSession): List<Førstegangsbehandling> {
        return tx.run(
            queryOf(
                sqlHentBehandlingForSak,
                mapOf(
                    "sakId" to sakId.toString(),
                ),
            ).map { row ->
                row.toBehandling(tx)
            }.asList,
        )
    }

    override fun lagre(behandling: Behandling, context: TransactionContext?): Behandling {
        return sessionFactory.withTransaction(context) { tx ->
            lagre(behandling, tx)
        }
    }

    override fun lagre(behandling: Behandling, tx: TransactionalSession): Behandling {
        val sistEndret = hentSistEndret(behandling.id, tx)
        return if (sistEndret == null) {
            opprettBehandling(behandling, tx)
        } else {
            oppdaterBehandling(sistEndret, behandling, tx)
        }.also {
            saksopplysningRepo.lagre(behandling.id, behandling.saksopplysninger, tx)
            if (behandling is Førstegangsbehandling) {
                søknadDAO.lagre(behandling.id, behandling.søknader, tx)
            }
            tiltakDAO.lagre(behandling.id, behandling.tiltak.tiltak, tx)
            vurderingRepo.lagre(behandling.id, behandling.vilkårsvurderinger, tx)
            utfallsperiodeDAO.lagre(behandling.id, behandling.utfallsperioder, tx)
        }
    }

    private fun oppdaterBehandling(
        sistEndret: LocalDateTime,
        behandling: Behandling,
        txSession: TransactionalSession,
    ): Behandling {
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
        return behandling
    }

    private fun opprettBehandling(
        behandling: Behandling,
        txSession: TransactionalSession,
    ): Behandling {
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
                    "opprettet" to behandling.opprettet,
                    "vilkaarssett" to behandling.vilkårssett.toDbJson(),
                ),
            ).asUpdate,
        )
        return behandling
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

    private fun Row.toBehandling(txSession: TransactionalSession): Førstegangsbehandling {
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
            "Vilkårsvurdert" -> BehandlingTilstand.VILKÅRSVURDERT
            "TilBeslutting" -> BehandlingTilstand.TIL_BESLUTTER
            "Iverksatt" -> BehandlingTilstand.IVERKSATT
            else -> throw IllegalStateException("Hentet en Behandling $id med ukjent status : $type")
        }
        val vilkårssett = string("vilkårssett").toVilkårssett(
            saksopplysninger = saksopplysningRepo.hent(id, txSession),
            vilkårsvurderinger = vurderingRepo.hent(id, txSession),
            utfallsperioder = utfallsperiodeDAO.hent(id, txSession),
        )
        return Førstegangsbehandling(
            id = id,
            sakId = sakId,
            søknader = søknadDAO.hent(id, txSession),
            vurderingsperiode = Periode(fom, tom),
            vilkårssett = vilkårssett,
            tiltak = TiltakVilkår(tiltakDAO.hent(id, txSession)),
            saksbehandler = saksbehandler,
            beslutter = beslutter,
            status = behandlingStatus,
            tilstand = tilstand,
            opprettet = localDateTime("opprettet"),
        )
    }

    private fun finnTilstand(behandling: Behandling): String =
        when (behandling.tilstand) {
            BehandlingTilstand.OPPRETTET -> "søknadsbehandling"
            BehandlingTilstand.VILKÅRSVURDERT -> "Vilkårsvurdert"
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
        select * from behandling where id = :id
    """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandlingForSak = """
        select * from behandling where sakId = :sakId
    """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandlingForIdent = """
        select * from behandling 
           where sakid = (
            select id 
             from sak 
             where ident = :ident)
    """.trimIndent()

    @Language("SQL")
    private val sqlHentBehandlingForJournalpostId = """
        select * from behandling 
         where id = 
            (select behandling_id 
             from søknad 
             where journalpost_id = :journalpostId)
    """.trimIndent()

    @Language("SQL")
    private val sqlHentAlleBehandlinger = """
        select * from behandling
    """.trimIndent()
}
