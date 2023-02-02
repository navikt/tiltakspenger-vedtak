package no.nav.tiltakspenger.vedtak.repository.uføre

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.UføreVedtakId
import no.nav.tiltakspenger.vedtak.UføreVedtak
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

class UføreVedtakDAO() {
    fun hentForInnsending(innsendingId: InnsendingId, txSession: TransactionalSession): UføreVedtak? {
        return txSession.run(
            queryOf(hentUføreVedtak, innsendingId.toString())
                .map { row -> row.toUføreVedtak(txSession) }
                .asSingle,
        )
    }

    fun lagre(
        innsendingId: InnsendingId,
        uføreVedtak: UføreVedtak?,
        txSession: TransactionalSession,
    ) {
        slettUføreVedtak(innsendingId, txSession)
        if (uføreVedtak == null) return
        lagreUføreVedtak(innsendingId, uføreVedtak, txSession)
    }

    private fun lagreUføreVedtak(
        innsendingId: InnsendingId,
        uføreVedtak: UføreVedtak,
        txSession: TransactionalSession,
    ) {
        val id = UføreVedtakId.random()
        txSession.run(
            queryOf(
                lagreUføreVedtak,
                mapOf(
                    "id" to id.toString(),
                    "innsendingId" to innsendingId.toString(),
                    "harUforegrad" to uføreVedtak.harUføregrad,
                    "datoUfor" to uføreVedtak.datoUfør,
                    "virkDato" to uføreVedtak.virkDato,
                    "innhentet" to uføreVedtak.innhentet,
                    "tidsstempelHosOss" to LocalDateTime.now(),
                ),
            ).asUpdate,
        )
    }

    private fun slettUføreVedtak(innsendingId: InnsendingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettUføreVedtak, innsendingId.toString()).asUpdate)
    }

    private fun Row.toUføreVedtak(txSession: TransactionalSession): UføreVedtak {
        return UføreVedtak(
            id = UføreVedtakId.fromDb(string("id")),
            harUføregrad = boolean("har_uforegrad"),
            datoUfør = localDateOrNull("dato_ufor"),
            virkDato = localDateOrNull("virk_dato"),
            innhentet = localDateTime("innhentet"),
        )
    }

    @Language("SQL")
    private val lagreUføreVedtak = """
        insert into uføre_vedtak (
            id,
            innsending_id,
            har_uforegrad,
            dato_ufor,
            virk_dato,
            innhentet,
            tidsstempel_hos_oss
        ) values (
            :id, 
            :innsendingId,
            :harUforegrad,
            :datoUfor,
            :virkDato,
            :innhentet,
            :tidsstempelHosOss
        )
    """.trimIndent()

    @Language("SQL")
    private val slettUføreVedtak = "delete from uføre_vedtak where innsending_id = ?"

    @Language("SQL")
    private val hentUføreVedtak = "select * from uføre_vedtak where innsending_id = ?"
}
