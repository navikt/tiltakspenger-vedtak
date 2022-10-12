package no.nav.tiltakspenger.vedtak.repository.ytelse

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.UlidBase
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseVedtak
import org.intellij.lang.annotations.Language

class YtelsevedtakDAO {

    fun hentForVedtak(ytelsesakId: UlidBase, txSession: TransactionalSession): List<YtelseVedtak> {
        return txSession.run(
            queryOf(hentYtelsevedtak, ytelsesakId.toString())
                .map { row -> row.toYtelsevedtak() }
                .asList
        )
    }

    fun lagre(ytelsesakId: UlidBase, ytelseVedtak: List<YtelseVedtak>, txSession: TransactionalSession) {
        // slettVedtak(ytelsesakId, txSession)
        ytelseVedtak.forEach { vedtak -> lagreVedtak(ytelsesakId, vedtak, txSession) }
    }

    private fun lagreVedtak(ytelsesakId: UlidBase, ytelseVedtak: YtelseVedtak, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                lagreYtelseVedtak, mapOf(
                    "id" to random(ULID_PREFIX_VEDTAK).toString(),
                    "ytelsesakId" to ytelsesakId.toString(),
                    "beslutningsDato" to ytelseVedtak.beslutningsDato,
                    "periodetypeForYtelse" to ytelseVedtak.periodetypeForYtelse?.name,
                    "vedtaksperiodeFom" to ytelseVedtak.vedtaksperiodeFom,
                    "vedtaksperiodeTom" to ytelseVedtak.vedtaksperiodeTom,
                    "vedtaksType" to ytelseVedtak.vedtaksType?.name,
                    "status" to ytelseVedtak.status?.name,
                )
            ).asUpdate
        )
    }

    private fun slettVedtak(ytelsesakId: UlidBase, txSession: TransactionalSession) {
        txSession.run(queryOf(slettYtelsevedtak, ytelsesakId.toString()).asUpdate)
    }

    fun slettVedtakForSøker(søkerId: SøkerId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettYtelsevedtak, søkerId.toString()).asUpdate)
    }

    private fun Row.toYtelsevedtak(): YtelseVedtak {
        return YtelseVedtak(
            beslutningsDato = localDateOrNull("beslutnings_dato"),
            periodetypeForYtelse = stringOrNull("periodetype_for_ytelse")?.let {
                YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse.valueOf(it)
            },
            vedtaksperiodeFom = localDateOrNull("vedtaksperiode_fom"),
            vedtaksperiodeTom = localDateOrNull("vedtaksperiode_tom"),
            vedtaksType = stringOrNull("vedtaks_type")?.let {
                YtelseVedtak.YtelseVedtakVedtakstype.valueOf(it)
            },
            status = stringOrNull("status")?.let {
                YtelseVedtak.YtelseVedtakStatus.valueOf(it)
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
        )""".trimIndent()

    @Language("SQL")
    private val slettYtelsevedtak =
        "delete from ytelsevedtak where ytelsesak_id in (select id from ytelsesak where søker_id = ?)"

    @Language("SQL")
    private val hentYtelsevedtak = "select * from ytelsevedtak where ytelsesak_id = ?"

    companion object {
        private const val ULID_PREFIX_VEDTAK = "aved"
    }
}
