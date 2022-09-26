package no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import org.intellij.lang.annotations.Language
import java.util.*

class TiltaksaktivitetDAO {
    fun hentForSøker(søkerId: UUID, txSession: TransactionalSession) : List<Tiltaksaktivitet> {
        return txSession.run(
            queryOf(hentTiltaksaktivitet, søkerId)
                .map { row ->
                    row.toTiltaksaktivitet(txSession)
                }.asList
        )
    }

    fun lagre(søkerId: UUID, tiltaksaktiviteter: List<Tiltaksaktivitet>, txSession: TransactionalSession) {
        slettTiltak(søkerId, txSession)
        tiltaksaktiviteter.forEach{ tiltaksaktivitet ->
            lagreTiltak(søkerId, tiltaksaktivitet, txSession)
        }
    }

    private fun lagreTiltak(søkerId: UUID, tiltaksaktivitet: Tiltaksaktivitet, txSession: TransactionalSession) {
        txSession.run (
            queryOf(
                lagreSøknad, mapOf(
                    "id" to UUID.randomUUID(),
                    "sokerId" to søkerId,
                    "tiltak" to tiltaksaktivitet.tiltak.name,
                    "aktivitetId" to tiltaksaktivitet.aktivitetId,
                    "deltakerStatus" to tiltaksaktivitet.deltakerStatus.name,
                    "innhentet" to tiltaksaktivitet.innhentet,
                )
            ).asUpdate
        )
    }

    private fun slettTiltak(søkerId: UUID, txSession: TransactionalSession) {
        txSession.run(
            queryOf(slettTiltaksaktivitet, søkerId).asUpdate
        )
    }

    private fun Row.toTiltaksaktivitet(txSession: TransactionalSession): Tiltaksaktivitet {
        val tiltak = string("tiltak").let { Tiltaksaktivitet.Tiltak.valueOf(it) }
        val deltakerStatus = string("deltaker_status").let { Tiltaksaktivitet.DeltakerStatus.valueOf(it) }
        val aktivitetId = string("aktivitet_id")
        val fom = localDateOrNull("fom")
        val tom = localDateOrNull("tom")
        val innhentet = localDateTime("innhentet")

        return Tiltaksaktivitet(
            tiltak = tiltak,
            aktivitetId = aktivitetId,
            tiltakLokaltNavn = null,
            arrangoer = null,
            bedriftsnummer = null,
            deltakelsePeriode = null,
            deltakelseProsent = null,
            deltakerStatus = deltakerStatus,
            statusSistEndret = null,
            begrunnelseInnsoeking = "",
            antallDagerPerUke = null,
            innhentet = innhentet,
        )
    }

    @Language("SQL")
    private val lagreSøknad = """
        insert into tiltaksaktivitet (
            id,
            søker_id,
            tiltak,
            aktivitet_id,
            deltaker_status,
            innhentet
        ) values (
            :id, 
            :sokerId,
            :tiltak,
            :aktivitetId,
            :deltakerStatus,
            :innhentet
        )""".trimIndent()

    @Language("SQL")
    private val slettTiltaksaktivitet = "delete from tiltaksaktivitet where søker_id = ?"

    @Language("SQL")
    private val hentTiltaksaktivitet = "select * from tiltaksaktivitet where søker_id = ?"
}
