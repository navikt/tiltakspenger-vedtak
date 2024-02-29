package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

// todo Må enten endres til å kunne hente og lagre alle typer behandlinger og ikke bare Søknadsbehandlinger
//      eller så må vi lage egne Repo for de andre type behandlingene
internal class PostgresBehandlingRepo(
    private val saksopplysningRepo: SaksopplysningRepo = SaksopplysningRepo(),
    private val vurderingRepo: VurderingRepo = VurderingRepo(),
    private val søknadDAO: SøknadDAO = SøknadDAO(),
    private val tiltakDAO: TiltakDAO = TiltakDAO(),
) : BehandlingRepo {
    override fun hent(behandlingId: BehandlingId): Søknadsbehandling? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
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
    }

    override fun hentAlle(): List<Søknadsbehandling> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentAlleBehandlinger,
                    ).map { row ->
                        row.toBehandling(txSession)
                    }.asList,
                )
            }
        }
    }

    override fun hentAlleForIdent(ident: String): List<Søknadsbehandling> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
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
    }

    override fun hentForSak(sakId: SakId): List<Søknadsbehandling> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentBehandlingForSak,
                        mapOf(
                            "sakId" to sakId.toString(),
                        ),
                    ).map { row ->
                        row.toBehandling(txSession)
                    }.asList,
                )
            }
        }
    }

    override fun hentForJournalpostId(journalpostId: String): Søknadsbehandling? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
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
    }

    override fun lagre(behandling: Søknadsbehandling): Søknadsbehandling {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                lagre(behandling, txSession)
            }
        }
    }

    override fun lagre(behandling: Søknadsbehandling, tx: TransactionalSession): Søknadsbehandling {
        val sistEndret = hentSistEndret(behandling.id, tx)
        return if (sistEndret == null) {
            opprettBehandling(behandling, tx)
        } else {
            oppdaterBehandling(sistEndret, behandling, tx)
        }.also {
            saksopplysningRepo.lagre(behandling.id, behandling.saksopplysninger, tx)
            søknadDAO.lagre(behandling.id, behandling.søknader, tx)
            tiltakDAO.lagre(behandling.id, behandling.tiltak, tx)
            when (behandling) {
                is BehandlingIverksatt -> {
                    vurderingRepo.lagre(behandling.id, behandling.vilkårsvurderinger, tx)
                }

                is BehandlingVilkårsvurdert -> {
                    vurderingRepo.lagre(behandling.id, behandling.vilkårsvurderinger, tx)
                }

                is BehandlingTilBeslutter -> {
                    vurderingRepo.lagre(behandling.id, behandling.vilkårsvurderinger, tx)
                }

                is Søknadsbehandling.Opprettet -> {}
            }
        }
    }

    private fun oppdaterBehandling(
        sistEndret: LocalDateTime,
        behandling: Søknadsbehandling,
        txSession: TransactionalSession,
    ): Søknadsbehandling {
        SECURELOG.info { "Oppdaterer behandling ${behandling.id}" }

        val antRaderOppdatert = txSession.run(
            queryOf(
                sqlOppdaterBehandling,
                mapOf(
                    "id" to behandling.id.toString(),
                    "sakId" to behandling.sakId.toString(),
                    "fom" to behandling.vurderingsperiode.fra,
                    "tom" to behandling.vurderingsperiode.til,
                    "tilstand" to finnTilstand(behandling),
                    "status" to finnStatus(behandling),
                    "sistEndretOld" to sistEndret,
                    "sistEndret" to nå(),
                    "saksbehandler" to behandling.saksbehandler,
                    "beslutter" to if (behandling is BehandlingTilBeslutter) behandling.beslutter else if (behandling is BehandlingIverksatt) behandling.beslutter else null,
                ),
            ).asUpdate,
        )
        if (antRaderOppdatert == 0) {
            throw IllegalStateException("Noen andre har endret denne behandlingen ${behandling.id}")
        }
        return behandling
    }

    private fun opprettBehandling(behandling: Søknadsbehandling, txSession: TransactionalSession): Søknadsbehandling {
        SECURELOG.info { "Oppretter behandling ${behandling.id}" }

        val nå = nå()

        txSession.run(
            queryOf(
                sqlOpprettBehandling,
                mapOf(
                    "id" to behandling.id.toString(),
                    "sakId" to behandling.sakId.toString(),
                    "fom" to behandling.vurderingsperiode.fra,
                    "tom" to behandling.vurderingsperiode.til,
                    "tilstand" to finnTilstand(behandling),
                    "status" to finnStatus(behandling),
                    "sistEndret" to nå,
                    "opprettet" to nå,
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

    private fun Row.toBehandling(txSession: TransactionalSession): Søknadsbehandling {
        val id = BehandlingId.fromDb(string("id"))
        val sakId = SakId.fromDb(string("sakId"))
        val fom = localDate("fom")
        val tom = localDate("tom")
        val status = string("status")
        val saksbehandler = stringOrNull("saksbehandler")
        val beslutter = stringOrNull("beslutter")
        return when (val type = string("tilstand")) {
            "søknadsbehandling" -> Søknadsbehandling.Opprettet.fromDb(
                id = id,
                sakId = sakId,
                søknader = søknadDAO.hent(id, txSession),
                vurderingsperiode = Periode(fom, tom),
                saksopplysninger = saksopplysningRepo.hent(id, txSession),
                tiltak = tiltakDAO.hent(id, txSession),
                saksbehandler = saksbehandler,
            )

            "Vilkårsvurdert" -> BehandlingVilkårsvurdert.fromDb(
                id = id,
                sakId = sakId,
                søknader = søknadDAO.hent(id, txSession),
                vurderingsperiode = Periode(fom, tom),
                saksopplysninger = saksopplysningRepo.hent(id, txSession),
                tiltak = tiltakDAO.hent(id, txSession),
                vilkårsvurderinger = vurderingRepo.hent(id, txSession),
                saksbehandler = saksbehandler,
                status = status,
                utfallsperioder = emptyList(),
            )

            "TilBeslutting" -> BehandlingTilBeslutter.fromDb(
                id = id,
                sakId = sakId,
                søknader = søknadDAO.hent(id, txSession),
                vurderingsperiode = Periode(fom, tom),
                saksopplysninger = saksopplysningRepo.hent(id, txSession),
                tiltak = tiltakDAO.hent(id, txSession),
                vilkårsvurderinger = vurderingRepo.hent(id, txSession),
                status = status,
                saksbehandler = checkNotNull(saksbehandler) { "Behandling som er til beslutning mangler saksbehandler i basen" },
                beslutter = beslutter,
            )

            "Iverksatt" -> BehandlingIverksatt.fromDb(
                id = id,
                sakId = sakId,
                søknader = søknadDAO.hent(id, txSession),
                vurderingsperiode = Periode(fom, tom),
                saksopplysninger = saksopplysningRepo.hent(id, txSession),
                tiltak = tiltakDAO.hent(id, txSession),
                vilkårsvurderinger = vurderingRepo.hent(id, txSession),
                status = status,
                saksbehandler = checkNotNull(saksbehandler) { "Behandling som er iverksatt mangler saksbehandler i basen" },
                beslutter = checkNotNull(beslutter) { "Behandling som er iverksatt mangler beslutter i basen" },
            )

            else -> throw IllegalStateException("Hentet en Behandling $id med ukjent status : $type")
        }
    }

    private fun finnTilstand(behandling: Søknadsbehandling) =
        when (behandling) {
            is Søknadsbehandling.Opprettet -> "søknadsbehandling"
            is BehandlingVilkårsvurdert -> "Vilkårsvurdert"
            is BehandlingTilBeslutter -> "TilBeslutting"
            is BehandlingIverksatt -> "Iverksatt"
        }

    private fun finnStatus(behandling: Søknadsbehandling) =
        when (behandling) {
            is Søknadsbehandling.Opprettet -> "Opprettet"
            is BehandlingVilkårsvurdert.Avslag -> "Avslag"
            is BehandlingVilkårsvurdert.Innvilget -> "Innvilget"
            is BehandlingVilkårsvurdert.Manuell -> "Manuell"
            is BehandlingIverksatt.Avslag -> "Avslag"
            is BehandlingIverksatt.Innvilget -> "Innvilget"
            is BehandlingTilBeslutter.Avslag -> "Avslag"
            is BehandlingTilBeslutter.Innvilget -> "Innvilget"
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
            opprettet
        ) values (
            :id,
            :sakId,
            :fom,
            :tom,
            :tilstand,
            :status,
            :sistEndret,
            :opprettet
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
            beslutter = :beslutter
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
