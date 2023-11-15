package no.nav.tiltakspenger.vedtak.repository.ytelse

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.UlidBase
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.vedtak.innsending.YtelseSak
import org.intellij.lang.annotations.Language

class YtelsevedtakDAO {

    fun hentForVedtak(ytelsesakId: UlidBase, txSession: TransactionalSession): List<YtelseSak.YtelseVedtak> {
        return txSession.run(
            queryOf(hentYtelsevedtak, ytelsesakId.toString())
                .map { row -> row.toYtelsevedtak() }
                .asList,
        )
    }

    fun lagre(ytelsesakId: UlidBase, ytelseVedtak: List<YtelseSak.YtelseVedtak>, txSession: TransactionalSession) {
        // slettVedtak(ytelsesakId, txSession)
        ytelseVedtak.forEach { vedtak -> lagreVedtak(ytelsesakId, vedtak, txSession) }
    }

    private fun lagreVedtak(ytelsesakId: UlidBase, ytelseVedtak: YtelseSak.YtelseVedtak, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                lagreYtelseVedtak,
                mapOf(
                    "id" to random(ULID_PREFIX_VEDTAK).toString(),
                    "ytelsesakId" to ytelsesakId.toString(),
                    "beslutningsDato" to ytelseVedtak.beslutningsDato,
                    "periodetypeForYtelse" to ytelseVedtak.periodetypeForYtelse?.name,
                    "vedtaksperiodeFom" to ytelseVedtak.vedtaksperiodeFom,
                    "vedtaksperiodeTom" to ytelseVedtak.vedtaksperiodeTom,
                    "vedtaksType" to ytelseVedtak.vedtaksType?.name,
                    "status" to ytelseVedtak.status?.name,
                ),
            ).asUpdate,
        )
    }

    fun slettVedtakForInnsending(innsendingId: InnsendingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettYtelsevedtak, innsendingId.toString()).asUpdate)
    }

    private fun Row.toYtelsevedtak(): YtelseSak.YtelseVedtak {
        return YtelseSak.YtelseVedtak(
            beslutningsDato = localDateOrNull("beslutnings_dato"),
            periodetypeForYtelse = stringOrNull("periodetype_for_ytelse")?.let {
                YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse.valueOf(it)
            },
            vedtaksperiodeFom = localDateOrNull("vedtaksperiode_fom"),
            vedtaksperiodeTom = localDateOrNull("vedtaksperiode_tom"),
            vedtaksType = stringOrNull("vedtaks_type")?.let {
                YtelseSak.YtelseVedtak.YtelseVedtakVedtakstype.valueOf(it)
            },
            status = stringOrNull("status")?.let {
                YtelseSak.YtelseVedtak.YtelseVedtakStatus.valueOf(it)
            },
        )
    }

    @Language("SQL")
    private val lagreYtelseVedtak = """
        insert into ytelsevedtak (
            id,
            ytelsesak_id,
            beslutnings_dato,
            periodetype_for_ytelse,
            vedtaksperiode_fom,
            vedtaksperiode_tom,
            vedtaks_type,
            status
        ) values (
            :id, 
            :ytelsesakId,
            :beslutningsDato,
            :periodetypeForYtelse,
            :vedtaksperiodeFom,
            :vedtaksperiodeTom,
            :vedtaksType,
            :status
        )
    """.trimIndent()

    @Language("SQL")
    private val slettYtelsevedtak =
        "delete from ytelsevedtak where ytelsesak_id in (select id from ytelsesak where innsending_id = ?)"

    @Language("SQL")
    private val hentYtelsevedtak = "select * from ytelsevedtak where ytelsesak_id = ?"

    companion object {
        private const val ULID_PREFIX_VEDTAK = "aved"
    }
}
