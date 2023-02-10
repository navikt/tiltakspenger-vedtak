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
        if (overgangsstønadVedtak == null || overgangsstønadVedtak.isEmpty()) return
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
                    "innsendingId" to innsendingId.toString(),
                    "fom_dato" to overgangsstønadVedtak.periode.fomDato,
                    "tom_dato" to overgangsstønadVedtak.periode.tomDato,
                    "innhentet" to overgangsstønadVedtak.innhentet,
                    "tidsstempelHosOss" to LocalDateTime.now(),
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
                fomDato = string("fom_dato"),
                tomDato = string("tom_dato"),
                datakilde = string("datakilde")
            ),
            innhentet = localDateTime("innhentet"),
        )
    }

    @Language("SQL")
    private val lagreOvergangsstønadVedtak = """
        insert into overgangsstønad_vedtak (
            id,
            fom_dato,
            tom_dato,
            datakilde,
            innhentet,
        ) values (
            :id, 
            :fom_dato,
            :tom_dato,
            :datakilde,
            :innhentet,
        )
    """.trimIndent()

    @Language("SQL")
    private val slettOvergangsstønadVedtak = "delete from overgangsstønad_vedtak where innsending_id = ?"

    @Language("SQL")
    private val hentOvergangsstønadVedtak = "select * from overgangsstønad_vedtak where innsending_id = ?"
}
