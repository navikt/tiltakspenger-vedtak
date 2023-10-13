package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
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
) : BehandlingRepo {
    override fun hent(behandlingId: BehandlingId): Søknadsbehandling? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        SqlHentBehandling,
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

    override fun hentForSak(sakId: SakId): List<Søknadsbehandling> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        SqlHentBehandlingForSak,
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

    override fun lagre(behandling: Søknadsbehandling): Søknadsbehandling {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                val sistEndret = hentSistEndret(behandling.id, txSession)
                if (sistEndret == null) {
                    opprettBehandling(behandling, txSession)
                } else {
                    oppdaterBehandling(sistEndret, behandling, txSession)
                }.also {
                    saksopplysningRepo.lagre(behandling.id, behandling.saksopplysninger, txSession)
                    søknadDAO.oppdaterBehandlingId(behandling.id, behandling.søknader, txSession)
                    when (behandling) {
                        is BehandlingIverksatt -> {
                            vurderingRepo.lagre(behandling.id, behandling.vilkårsvurderinger, txSession)
                        }

                        is BehandlingVilkårsvurdert -> {
                            vurderingRepo.lagre(behandling.id, behandling.vilkårsvurderinger, txSession)
                        }

                        is Søknadsbehandling.Opprettet -> {}
                    }
                }
            }
        }
        return behandling
    }

    private fun oppdaterBehandling(
        sistEndret: LocalDateTime,
        behandling: Søknadsbehandling,
        txSession: TransactionalSession,
    ): Søknadsbehandling {
        SECURELOG.info { "Oppdaterer behandling ${behandling.id}" }

        val antRaderOppdatert = txSession.run(
            queryOf(
                SqlOppdaterBehandling,
                mapOf(
                    "id" to behandling.id.toString(),
                    "sakId" to behandling.sakId.toString(),
                    "fom" to behandling.vurderingsperiode.fra,
                    "tom" to behandling.vurderingsperiode.til,
                    "tilstand" to finnTilstand(behandling),
                    "status" to finnStatus(behandling),
                    "sistEndretOld" to sistEndret,
                    "sistEndret" to nå(),
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
        return when (val type = string("tilstand")) {
            "søknadsbehandling" -> Søknadsbehandling.Opprettet.fromDb(
                id = id,
                sakId = sakId,
                søknader = søknadDAO.hentMedBehandlingId(id, txSession),
                vurderingsperiode = Periode(fom, tom),
                saksopplysninger = saksopplysningRepo.hent(id, txSession),
            )

            "Vilkårsvurdert" -> BehandlingVilkårsvurdert.fromDb(
                id = id,
                sakId = sakId,
                søknader = søknadDAO.hentMedBehandlingId(id, txSession),
                vurderingsperiode = Periode(fom, tom),
                saksopplysninger = saksopplysningRepo.hent(id, txSession),
                vilkårsvurderinger = vurderingRepo.hent(id, txSession),
                status = status,
            )

            "Iverksatt" -> BehandlingIverksatt.fromDb(
                id = id,
                sakId = sakId,
                søknader = søknadDAO.hentMedBehandlingId(id, txSession),
                vurderingsperiode = Periode(fom, tom),
                saksopplysninger = saksopplysningRepo.hent(id, txSession),
                vilkårsvurderinger = vurderingRepo.hent(id, txSession),
                status = status,
                saksbehandler = string("saksbehandler"),
            )

            else -> throw IllegalStateException("Hentet en Behandling $id med ukjent status : $type")
        }
    }

    private fun finnTilstand(behandling: Søknadsbehandling) =
        when (behandling) {
            is Søknadsbehandling.Opprettet -> "søknadsbehandling"
            is BehandlingVilkårsvurdert -> "Vilkårsvurdert"
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
    private val SqlOppdaterBehandling = """
        update behandling set 
            fom = :fom,
            tom = :tom,
            sakId = :sakId,
            tilstand = :tilstand,
            status = :status,
            sist_endret = :sistEndret
        where id = :id
          and sist_endret = :sistEndretOld
    """.trimIndent()

    @Language("SQL")
    private val SqlHentBehandling = """
        select * from behandling where id = :id
    """.trimIndent()

    @Language("SQL")
    private val SqlHentBehandlingForSak = """
        select * from behandling where sakId = :sakId
    """.trimIndent()

//    override fun hent(behandlingId: BehandlingId): Behandling? {
//
//
//        // TODO: Denne skal ikke opprette behandling på sikt, men skal hente ut fra databasen.
//        return Søknadsbehandling.Opprettet.opprettBehandling(
//            søknad = ObjectMother.nySøknadMedTiltak(),
//        )
//    }
}
