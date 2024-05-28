package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import org.intellij.lang.annotations.Language

class TiltakDAO {

    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): List<Tiltak> {
        return txSession.run(
            queryOf(hentTiltakSql, behandlingId.toString())
                .map { row ->
                    row.toTiltak(behandlingId = behandlingId, txSession = txSession)
                }
                .asList,
        )
    }

    fun lagre(behandlingId: BehandlingId, tiltakListe: List<Tiltak>, txSession: TransactionalSession) {
        slettStønadsdager(behandlingId, txSession)
        slettTiltak(behandlingId, txSession)
        tiltakListe.forEach { tiltak ->
            lagreTiltak(behandlingId, tiltak, txSession)
            lagreStønadsdager(behandlingId, tiltak, txSession)
        }
    }

    private fun hentAntallDager(
        behandlingId: BehandlingId,
        tiltakId: TiltakId,
        txSession: TransactionalSession,
    ): List<PeriodeMedVerdi<AntallDager>> {
        return txSession.run(
            queryOf(
                hentAntallDagerSql,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "tiltakId" to tiltakId.toString(),
                ),
            ).map { row -> row.toStønadsdager() }.asList,
        )
    }

    private fun hentAvklarteAntallDager(
        behandlingId: BehandlingId,
        tiltakId: TiltakId,
        txSession: TransactionalSession,
    ): List<PeriodeMedVerdi<AntallDager>> {
        return txSession.run(
            queryOf(
                hentAvklarteAntallDagerSql,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "tiltakId" to tiltakId.toString(),
                ),
            ).map { row -> row.toStønadsdager() }.asList,
        )
    }

    private fun lagreTiltak(
        behandlingId: BehandlingId,
        tiltak: Tiltak,
        txSession: TransactionalSession,
    ) {
        txSession.run(
            queryOf(
                lagreTiltakSql,
                mapOf(
                    "id" to tiltak.id.toString(),
                    "behandlingId" to behandlingId.toString(),
                    "eksternId" to tiltak.eksternId,
                    "gjennomforingId" to tiltak.gjennomføring.id,
                    "tiltaktypeKode" to tiltak.gjennomføring.typeKode,
                    "tiltaktypeNavn" to tiltak.gjennomføring.typeNavn,
                    "arrangornavn" to tiltak.gjennomføring.arrangørnavn,
                    "rettPaTiltakspenger" to tiltak.gjennomføring.rettPåTiltakspenger,
                    "deltakelseFom" to tiltak.deltakelseFom,
                    "deltakelseTom" to tiltak.deltakelseTom,
                    "deltakelseProsent" to tiltak.deltakelseProsent,
                    "deltakelseDagerUke" to tiltak.deltakelseDagerUke,
                    "deltakerStatus" to tiltak.deltakelseStatus.status,
                    "rettTilASoke" to tiltak.deltakelseStatus.rettTilÅASøke,
                    "kilde" to tiltak.kilde,
                    "tidsstempelKilde" to tiltak.registrertDato,
                    "tidsstempelHosOss" to tiltak.innhentet,
                ),
            ).asUpdate,
        )
    }

    private fun lagreStønadsdager(
        behandlingId: BehandlingId,
        tiltak: Tiltak,
        txSession: TransactionalSession,
        kilde: Kilde = Kilde.ARENA,
    ) {
        txSession.run(
            queryOf(
                lagreStønadsdagerSql,
                mapOf(
                    "id" to random(ULID_PREFIX_STØNADSDAGER).toString(),
                    "antallDager" to tiltak.deltakelseDagerUke,
                    "fom" to tiltak.deltakelseFom,
                    "tom" to tiltak.deltakelseTom,
                    "datakilde" to kilde.toString(),
                    "tidsstempelKilde" to tiltak.registrertDato,
                    "tidsstempelHosOss" to tiltak.innhentet,
                    "tiltakId" to tiltak.id.toString(),
                    "behandlingId" to behandlingId.toString(),
                ),
            ).asUpdate,
        )
    }

    private fun slettTiltak(behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettTiltakSql, behandlingId.toString()).asUpdate)
    }

    private fun slettStønadsdager(behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettStønadsdagerSql, behandlingId.toString()).asUpdate)
    }

    private fun Row.toTiltak(behandlingId: BehandlingId, txSession: TransactionalSession): Tiltak {
        return Tiltak(
            id = TiltakId.fromDb(string("id")),
            eksternId = string("ekstern_id"),
            gjennomføring = Tiltak.Gjennomføring(
                id = string("gjennomføring_id"),
                typeKode = string("tiltaktype_kode"),
                typeNavn = string("tiltaktype_navn"),
                arrangørnavn = string("arrangørnavn"),
                rettPåTiltakspenger = boolean("rett_på_tiltakspenger"),
            ),
            deltakelseFom = localDate("deltakelse_fom"),
            deltakelseTom = localDate("deltakelse_tom"),
            deltakelseStatus = Tiltak.DeltakerStatus(
                status = string("deltakelse_status"),
                rettTilÅASøke = boolean("rett_til_å_søke"),
            ),
            deltakelseDagerUke = floatOrNull("deltakelse_dager_uke"),
            deltakelseProsent = floatOrNull("deltakelse_prosent"),
            kilde = string("kilde"),
            registrertDato = localDateTime("tidsstempel_kilde"),
            innhentet = localDateTime("tidsstempel_hos_oss"),
            antallDagerSaksopplysninger = AntallDagerSaksopplysninger.initAntallDagerSaksopplysning(
                antallDager = hentAntallDager(behandlingId, TiltakId.fromDb(string("id")), txSession),
                avklarteAntallDager = hentAvklarteAntallDager(behandlingId, TiltakId.fromDb(string("id")), txSession),
            ),
        )
    }

    private fun Row.toStønadsdager(): PeriodeMedVerdi<AntallDager> {
        return PeriodeMedVerdi(
            periode = Periode(
                fra = localDate("fom"),
                til = localDate("tom"),
            ),
            verdi = AntallDager(
                antallDager = int("antall_dager"),
                kilde = Kilde.valueOf(string("datakilde")),
            ),
        )
    }

    @Language("SQL")
    private val lagreStønadsdagerSql = """
        insert into stønadsdager_tiltak (
            id,
            antall_dager,
            fom,
            tom,
            datakilde,
            tidsstempel_kilde,
            tidsstempel_hos_oss,
            tiltak_id,
            behandling_id
        ) values (
            :id,
            :antallDager,
            :fom,
            :tom,
            :datakilde,
            :tidsstempelKilde,
            :tidsstempelHosOss,
            :tiltakId,
            :behandlingId
        )
    """.trimIndent()

    @Language("SQL")
    private val lagreTiltakSql = """
        insert into tiltak (
            id,
            behandling_id,
            ekstern_id,
            gjennomføring_id,
            tiltaktype_kode,
            tiltaktype_navn,
            arrangørnavn,
            rett_på_tiltakspenger,
            deltakelse_fom,
            deltakelse_tom,
            deltakelse_prosent,
            deltakelse_dager_uke,
            deltakelse_status,
            rett_til_å_søke,
            kilde,
            tidsstempel_kilde,
            tidsstempel_hos_oss
        ) values (
            :id,
            :behandlingId,
            :eksternId,
            :gjennomforingId,
            :tiltaktypeKode,
            :tiltaktypeNavn,
            :arrangornavn,
            :rettPaTiltakspenger,            
            :deltakelseFom,
            :deltakelseTom,
            :deltakelseProsent,
            :deltakelseDagerUke,
            :deltakerStatus,
            :rettTilASoke,
            :kilde,
            :tidsstempelKilde,
            :tidsstempelHosOss
        )
    """.trimIndent()

    @Language("SQL")
    private val slettTiltakSql = "delete from tiltak where behandling_id = ?"

    @Language("SQL")
    private val slettStønadsdagerSql = "delete from stønadsdager_tiltak where behandling_id = ?"

    @Language("SQL")
    private val hentTiltakSql = "select * from tiltak where behandling_id = ?"

    @Language("SQL")
    private val hentAntallDagerSql =
        "select * from stønadsdager_tiltak where behandling_id = :behandlingId and tiltak_id = :tiltakId"

    @Language("SQL")
    private val hentAvklarteAntallDagerSql =
        "select * from stønadsdager_tiltak where behandling_id = :behandlingId and tiltak_id = :tiltakId AND avklart_tidspunkt IS NOT NULL"

    companion object {
        private const val ULID_PREFIX_TILTAK = "takt"
        private const val ULID_PREFIX_STØNADSDAGER = "stond"
    }
}
