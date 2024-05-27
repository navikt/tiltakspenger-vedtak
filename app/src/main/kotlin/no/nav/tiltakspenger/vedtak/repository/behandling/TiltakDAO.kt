package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedKildeOgVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import org.intellij.lang.annotations.Language

class TiltakDAO {

    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): List<Tiltak> {
        return txSession.run(
            queryOf(hentTiltakSql, behandlingId.toString())
                .map { row ->
                    val tiltak = row.toTiltak()
                    val antallDager = hentAntallDager(behandlingId, tiltak, txSession)
                    val avklarteAntallDager = hentAvklarteAntallDager(behandlingId, tiltak, txSession)
                    tiltak.copy(
                        antallDagerSaksopplysninger = AntallDagerSaksopplysninger.initAntallDagerSaksopplysning(antallDager, avklarteAntallDager),
                    )
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
        tiltak: Tiltak,
        txSession: TransactionalSession,

    ): List<PeriodeMedKildeOgVerdi<Int>> {
        return txSession.run(
            queryOf(
                hentAntallDagerSql,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "tiltakId" to tiltak.id,
                ),
            ).map { row -> row.toStønadsdager() }.asList,
        )
    }

    private fun hentAvklarteAntallDager(
        behandlingId: BehandlingId,
        tiltak: Tiltak,
        txSession: TransactionalSession,

    ): List<PeriodeMedKildeOgVerdi<Int>> {
        return txSession.run(
            queryOf(
                hentAvklarteAntallDagerSql,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "tiltakId" to tiltak.id,
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
                    "id" to random(ULID_PREFIX_TILTAK).toString(),
                    "behandlingId" to behandlingId.toString(),
                    "eksternId" to tiltak.id,
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
                    "datakilde" to kilde,
                    "tidsstempel" to tiltak.innhentet,
                    "tiltakId" to tiltak.id,
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

    private fun Row.toTiltak(): Tiltak {
        return Tiltak(
            id = string("ekstern_id"),
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
            antallDagerSaksopplysninger = AntallDagerSaksopplysninger(
                // TODO: Vi må se på lagringen før vi finner ut av hvordan vi kan hente ut data om antall dager fra db
                antallDagerSaksopplysningerFraRegister = emptyList(),
            ),
        )
    }

    private fun Row.toStønadsdager(): PeriodeMedKildeOgVerdi<Int> {
        return PeriodeMedKildeOgVerdi(
            periode = Periode(
                fra = localDate("fom"),
                til = localDate("tom"),
            ),
            verdi = int("antall_dager"),
            kilde = string("datakilde"),
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
            tidsstempel,
            tiltak_id,
            behandling_id
        ) values (
            :id,
            :antallDager,
            :fom,
            :tom,
            :datakilde,
            :tidsstempel,
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
    private val hentAntallDagerSql = "select * from tiltak_stonadsdager where behandling_id = ? and tiltak_id = ? AND datakilde = ?"

    @Language("SQL")
    private val hentAvklarteAntallDagerSql = "select * from tiltak_stonadsdager where behandling_id = ? and tiltak_id = ? AND avklart_tidspunkt IS NOT NULL"

    companion object {
        private const val ULID_PREFIX_TILTAK = "takt"
        private const val ULID_PREFIX_STØNADSDAGER = "stond"
    }
}
