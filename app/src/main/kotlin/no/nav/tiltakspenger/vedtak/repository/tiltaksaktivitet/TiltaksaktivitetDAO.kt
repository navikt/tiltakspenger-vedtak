package no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import org.intellij.lang.annotations.Language

class TiltaksaktivitetDAO {

    fun hentForInnsending(innsendingId: InnsendingId, txSession: TransactionalSession): List<Tiltaksaktivitet> {
        return txSession.run(
            queryOf(hentTiltaksaktivitet, innsendingId.toString())
                .map { row -> row.toTiltaksaktivitet() }
                .asList
        )
    }

    fun lagre(innsendingId: InnsendingId, tiltaksaktiviteter: List<Tiltaksaktivitet>, txSession: TransactionalSession) {
        slettTiltak(innsendingId, txSession)
        tiltaksaktiviteter.forEach { tiltaksaktivitet ->
            lagreTiltak(innsendingId, tiltaksaktivitet, txSession)
        }
    }

    private fun lagreTiltak(
        innsendingId: InnsendingId,
        tiltaksaktivitet: Tiltaksaktivitet,
        txSession: TransactionalSession
    ) {
        txSession.run(
            queryOf(
                lagreTiltaksaktivitet, mapOf(
                    "id" to random(ULID_PREFIX_TILTAKSAKTIVITET).toString(),
                    "innsendingId" to innsendingId.toString(),
                    "tiltak" to tiltaksaktivitet.tiltak.name,
                    "aktivitetId" to tiltaksaktivitet.aktivitetId,
                    "tiltakLokaltNavn" to tiltaksaktivitet.tiltakLokaltNavn,
                    "arrangor" to tiltaksaktivitet.arrangør,
                    "bedriftsnummer" to tiltaksaktivitet.bedriftsnummer,
                    "deltakelsePeriodeFom" to tiltaksaktivitet.deltakelsePeriode.fom,
                    "deltakelsePeriodeTom" to tiltaksaktivitet.deltakelsePeriode.tom,
                    "deltakelseProsent" to tiltaksaktivitet.deltakelseProsent,
                    "deltakerStatus" to tiltaksaktivitet.deltakerStatus.name,
                    "statusSistEndret" to tiltaksaktivitet.statusSistEndret,
                    "begrunnelseInnsoking" to tiltaksaktivitet.begrunnelseInnsøking,
                    "antallDagerPerUke" to tiltaksaktivitet.antallDagerPerUke,
                    "tidsstempelHosOss" to tiltaksaktivitet.tidsstempelHosOss
                )
            ).asUpdate
        )
    }

    private fun slettTiltak(innsendingId: InnsendingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettTiltaksaktivitet, innsendingId.toString()).asUpdate)
    }

    private fun Row.toTiltaksaktivitet(): Tiltaksaktivitet {
        return Tiltaksaktivitet(
            tiltak = string("tiltak").let { Tiltaksaktivitet.Tiltak.valueOf(it) },
            aktivitetId = string("aktivitet_id"),
            tiltakLokaltNavn = stringOrNull("tiltak_lokalt_navn"),
            arrangør = stringOrNull("arrangør"),
            bedriftsnummer = stringOrNull("bedriftsnummer"),
            deltakelsePeriode = Tiltaksaktivitet.DeltakelsesPeriode(
                fom = localDateOrNull("deltakelse_periode_fom"),
                tom = localDateOrNull("deltakelse_periode_tom")
            ),
            deltakelseProsent = floatOrNull("deltakelse_prosent"),
            deltakerStatus = string("deltaker_status").let { Tiltaksaktivitet.DeltakerStatus.valueOf(it) },
            statusSistEndret = localDateOrNull("status_sist_endret"),
            begrunnelseInnsøking = stringOrNull("begrunnelse_innsøking"),
            antallDagerPerUke = floatOrNull("antall_dager_per_uke"),
            tidsstempelHosOss = localDateTime("tidsstempel_hos_oss"),
        )
    }

    @Language("SQL")
    private val lagreTiltaksaktivitet = """
        insert into tiltaksaktivitet (
            id,
            innsending_id,
            tiltak,
            aktivitet_id,
            tiltak_lokalt_navn,
            arrangør,
            bedriftsnummer,
            deltakelse_periode_fom,
            deltakelse_periode_tom,
            deltakelse_prosent,
            deltaker_status,
            status_sist_endret,
            begrunnelse_innsøking,
            antall_dager_per_uke,
            tidsstempel_hos_oss
        ) values (
            :id, 
            :innsendingId,
            :tiltak,
            :aktivitetId,
            :tiltakLokaltNavn,
            :arrangor,
            :bedriftsnummer,
            :deltakelsePeriodeFom,
            :deltakelsePeriodeTom,
            :deltakelseProsent,
            :deltakerStatus,
            :statusSistEndret,
            :begrunnelseInnsoking,
            :antallDagerPerUke,
            :tidsstempelHosOss
        )""".trimIndent()

    @Language("SQL")
    private val slettTiltaksaktivitet = "delete from tiltaksaktivitet where innsending_id = ?"

    @Language("SQL")
    private val hentTiltaksaktivitet = "select * from tiltaksaktivitet where innsending_id = ?"

    companion object {
        private const val ULID_PREFIX_TILTAKSAKTIVITET = "takt"
    }
}
