package no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import org.intellij.lang.annotations.Language
import java.util.*

class TiltaksaktivitetDAO {
    fun hentForSøker(søkerId: UUID, txSession: TransactionalSession): List<Tiltaksaktivitet> {
        return txSession.run(
            queryOf(hentTiltaksaktivitet, søkerId)
                .map { row ->
                    row.toTiltaksaktivitet()
                }.asList
        )
    }

    fun lagre(søkerId: UUID, tiltaksaktiviteter: List<Tiltaksaktivitet>, txSession: TransactionalSession) {
        slettTiltak(søkerId, txSession)
        tiltaksaktiviteter.forEach { tiltaksaktivitet ->
            lagreTiltak(søkerId, tiltaksaktivitet, txSession)
        }
    }

    private fun lagreTiltak(søkerId: UUID, tiltaksaktivitet: Tiltaksaktivitet, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                lagreSøknad, mapOf(
                    "id" to UUID.randomUUID(),
                    "sokerId" to søkerId,
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

    private fun slettTiltak(søkerId: UUID, txSession: TransactionalSession) {
        txSession.run(
            queryOf(slettTiltaksaktivitet, søkerId).asUpdate
        )
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
    private val lagreSøknad = """
        insert into tiltaksaktivitet (
            id,
            søker_id,
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
            :sokerId,
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
    private val slettTiltaksaktivitet = "delete from tiltaksaktivitet where søker_id = ?"

    @Language("SQL")
    private val hentTiltaksaktivitet = "select * from tiltaksaktivitet where søker_id = ?"
}
