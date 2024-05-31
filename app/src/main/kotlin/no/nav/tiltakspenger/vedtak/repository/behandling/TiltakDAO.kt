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
import java.time.LocalDateTime

class TiltakDAO {

    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): List<Tiltak> {
        return txSession.run(
            queryOf(hentTiltakSql, behandlingId.toString())
                .map { row ->
                    val tiltak = row.toTiltak(behandlingId = behandlingId, txSession = txSession)
                    tiltak
                }
                .asList,
        )
    }

    fun lagre(behandlingId: BehandlingId, tiltakListe: List<Tiltak>, txSession: TransactionalSession) {
        slettStønadsdager(behandlingId, txSession)
        slettTiltak(behandlingId, txSession)
        tiltakListe.forEach { tiltak ->
            lagreTiltak(behandlingId, tiltak, txSession)
            if (tiltak.antallDagerSaksopplysninger.antallDagerSaksopplysningerFraRegister != tiltak.antallDagerSaksopplysninger.avklartAntallDager) {
                tiltak.antallDagerSaksopplysninger.antallDagerSaksopplysningerFraRegister.forEach { antallDager ->
                    lagreStønadsdager(behandlingId, tiltak, antallDager, txSession)
                }
            }
            if (tiltak.antallDagerSaksopplysninger.antallDagerSaksopplysningerFraSBH != tiltak.antallDagerSaksopplysninger.avklartAntallDager) {
                tiltak.antallDagerSaksopplysninger.antallDagerSaksopplysningerFraSBH.forEach { antallDager ->
                    lagreStønadsdager(behandlingId, tiltak, antallDager, txSession)
                }
            }
            tiltak.antallDagerSaksopplysninger.avklartAntallDager.forEach { antallDager ->
                lagreStønadsdager(behandlingId, tiltak, antallDager, txSession, avklart = true)
            }
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
        antallDagerMedPeriode: PeriodeMedVerdi<AntallDager>,
        txSession: TransactionalSession,
        avklart: Boolean = false,
    ) {
        val periode = antallDagerMedPeriode.periode
        val antallDagerSaksopplysning = antallDagerMedPeriode.verdi
        txSession.run(
            queryOf(
                lagreStønadsdagerSql,
                mapOf(
                    "id" to random(ULID_PREFIX_STØNADSDAGER).toString(),
                    "antallDager" to antallDagerSaksopplysning.antallDager,
                    "fom" to periode.fra,
                    "tom" to periode.til,
                    "datakilde" to antallDagerSaksopplysning.kilde.toString(),
                    "tidsstempelKilde" to tiltak.registrertDato,
                    "tidsstempelHosOss" to tiltak.innhentet,
                    "tiltakId" to tiltak.id.toString(),
                    "behandlingId" to behandlingId.toString(),
                    "saksbehandler" to antallDagerSaksopplysning.saksbehandlerIdent,
                    "avklartTidspunkt" to if (avklart) LocalDateTime.now() else null,
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
                kilde = Kilde.valueOf(string("datakilde").uppercase()),
                saksbehandlerIdent = stringOrNull("saksbehandler"),
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
            behandling_id,
            saksbehandler,
            avklart_tidspunkt
        ) values (
            :id,
            :antallDager,
            :fom,
            :tom,
            :datakilde,
            :tidsstempelKilde,
            :tidsstempelHosOss,
            :tiltakId,
            :behandlingId,
            :saksbehandler,
            :avklartTidspunkt
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
