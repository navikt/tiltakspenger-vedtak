package no.nav.tiltakspenger.vedtak.repository.overgangsstønad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import no.nav.tiltakspenger.vedtak.OvergangsstønadPeriode
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

class OvergangsstønadVedtakDAO() {
    fun hentForInnsending(innsendingId: InnsendingId, txSession: TransactionalSession): List<OvergangsstønadVedtak> {
        return txSession.run(
            queryOf(hentOvergangsstønadVedtak, innsendingId.toString())
                .map { row -> row.toOvergangsstønadVedtak() }
                .asList,
        )
    }

    fun lagre(
        innsendingId: InnsendingId,
        overgangsstønadVedtak: List<OvergangsstønadVedtak>,
        txSession: TransactionalSession,
    ) {
        slettOvergangsstønadVedtak(innsendingId, txSession)
        if (overgangsstønadVedtak.isEmpty()) return
        overgangsstønadVedtak.forEach {
            lagreOvergangsstønadVedtak(innsendingId, it, txSession)
        }
    }

    private fun lagreOvergangsstønadVedtak(
        innsendingId: InnsendingId,
        overgangsstønadVedtak: OvergangsstønadVedtak,
        txSession: TransactionalSession,
    ) {
        val id = OvergangsstønadVedtakId.random()
        txSession.run(
            queryOf(
                lagreOvergangsstønadVedtak,
                mapOf(
                    "id" to id.toString(),
                    "innsending_id" to innsendingId.toString(),
                    "fom" to overgangsstønadVedtak.periode.fom,
                    "tom" to overgangsstønadVedtak.periode.tom,
                    "innhentet" to overgangsstønadVedtak.innhentet,
                    "tidsstempel_hos_oss" to LocalDateTime.now(),
                ),
            ).asUpdate,
        )
    }

    private fun slettOvergangsstønadVedtak(innsendingId: InnsendingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettOvergangsstønadVedtak, innsendingId.toString()).asUpdate)
    }

    private fun Row.toOvergangsstønadVedtak(): OvergangsstønadVedtak {
        return OvergangsstønadVedtak(
            id = OvergangsstønadVedtakId.fromDb(string("id")),
            periode = OvergangsstønadPeriode(
                fom = string("fom"),
                tom = string("tom"),
                datakilde = string("datakilde"),
            ),
            innhentet = localDateTime("innhentet"),
        )
    }

    @Language("SQL")
    private val lagreOvergangsstønadVedtak = """
        insert into overgangsstønad_vedtak (
            id,
            innsending_id,
            fom,
            tom,
            datakilde,
            innhentet,
            tidsstempel_hos_oss
        ) values (
            :id, 
            :innsending_id,
            :fom,
            :tom,
            :datakilde,
            :innhentet,
            :tidsstempel_hos_oss
        )
    """.trimIndent()

    @Language("SQL")
    private val slettOvergangsstønadVedtak = "delete from overgangsstønad_vedtak where innsending_id = ?"

    @Language("SQL")
    private val hentOvergangsstønadVedtak = "select * from overgangsstønad_vedtak where innsending_id = ?"
}
