package no.nav.tiltakspenger.vedtak.repository.overgangsstønad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val SECURELOG = KotlinLogging.logger("tjenestekall")

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
        SECURELOG.info { "Skal lagre Overgangsstønader $overgangsstønadVedtak" }
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
        SECURELOG.info { "lagrer Overgangsstønad : $overgangsstønadVedtak" }
        val id = OvergangsstønadVedtakId.random()
        txSession.run(
            queryOf(
                lagreOvergangsstønadVedtak,
                mapOf(
                    "id" to id.toString(),
                    "innsending_id" to innsendingId.toString(),
                    "fom" to overgangsstønadVedtak.fom,
                    "tom" to overgangsstønadVedtak.tom,
                    "datakilde" to overgangsstønadVedtak.datakilde,
                    "innhentet" to overgangsstønadVedtak.innhentet,
                    "tidsstempel_hos_oss" to LocalDateTime.now(),
                ),
            ).asUpdate,
        )
    }

    private fun slettOvergangsstønadVedtak(innsendingId: InnsendingId, txSession: TransactionalSession) {
        SECURELOG.info { "Sletter overgangsstønad før insert" }
        txSession.run(queryOf(slettOvergangsstønadVedtak, innsendingId.toString()).asUpdate)
    }

    private fun Row.toOvergangsstønadVedtak(): OvergangsstønadVedtak {
        return OvergangsstønadVedtak(
            id = OvergangsstønadVedtakId.fromDb(string("id")),
            fom = localDate("fom"),
            tom = localDate("tom"),
            datakilde = string("datakilde"),
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
