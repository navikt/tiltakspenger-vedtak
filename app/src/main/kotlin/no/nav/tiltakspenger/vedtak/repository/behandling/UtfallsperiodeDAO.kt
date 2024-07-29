package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import org.intellij.lang.annotations.Language

class UtfallsperiodeDAO {

    fun hent(behandlingId: BehandlingId, session: Session): List<Utfallsperiode> {
        return session.run(
            queryOf(hentUtfallsperioder, behandlingId.toString())
                .map { row -> row.toUtfallsperiode() }
                .asList,
        )
    }

    fun hentForVedtak(vedtakId: VedtakId, txSession: TransactionalSession): List<Utfallsperiode> {
        return txSession.run(
            queryOf(hentUtfallsperioderForVedtak, vedtakId.toString())
                .map { row -> row.toUtfallsperiode() }
                .asList,
        )
    }

    fun oppdaterVedtak(vedtakId: VedtakId, behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                oppdaterVedtakId,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "vedtakId" to vedtakId.toString(),
                ),
            ).asUpdate,
        )
    }

    fun lagre(behandlingId: BehandlingId, utfallsperioder: List<Utfallsperiode>, txSession: TransactionalSession) {
        slettUtfallsperioder(behandlingId, txSession)
        utfallsperioder.forEach { utfallsperiode ->
            lagreUtfallsperiode(behandlingId, utfallsperiode, txSession)
        }
    }

    private fun lagreUtfallsperiode(
        behandlingId: BehandlingId,
        utfallsperiode: Utfallsperiode,
        txSession: TransactionalSession,
    ) {
        txSession.run(
            queryOf(
                lagreUtfallsperiode,
                mapOf(
                    "id" to random(ULID_PREFIX_UTFALLSPERIODE).toString(),
                    "behandlingId" to behandlingId.toString(),
                    "fom" to utfallsperiode.fom,
                    "tom" to utfallsperiode.tom,
                    "antallBarn" to utfallsperiode.antallBarn,
                    "utfall" to utfallsperiode.utfall.name,
                ),
            ).asUpdate,
        )
    }

    private fun slettUtfallsperioder(behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettUtfallsperioder, behandlingId.toString()).asUpdate)
    }

    private fun Row.toUtfallsperiode(): Utfallsperiode {
        return Utfallsperiode(
            fom = localDate("fom"),
            tom = localDate("tom"),
            antallBarn = int("antall_barn"),
            utfall = UtfallForPeriode.valueOf(string("utfall")),
        )
    }

    @Language("SQL")
    private val oppdaterVedtakId = """
        update utfallsperiode set 
            vedtak_id = :vedtakId
        where behandling_id = :behandlingId
    """.trimIndent()

    @Language("SQL")
    private val lagreUtfallsperiode = """
        insert into utfallsperiode (
            id,
            behandling_id,
            fom,
            tom,
            antall_barn,
            utfall
        ) values (
            :id,
            :behandlingId,
            :fom,
            :tom,
            :antallBarn,
            :utfall
        )
    """.trimIndent()

    @Language("SQL")
    private val slettUtfallsperioder = "delete from utfallsperiode where behandling_id = ?"

    @Language("SQL")
    private val hentUtfallsperioder = "select * from utfallsperiode where behandling_id = ?"

    @Language("SQL")
    private val hentUtfallsperioderForVedtak = "select * from utfallsperiode where vedtak_id = ?"

    companion object {
        private const val ULID_PREFIX_UTFALLSPERIODE = "uperiode"
    }
}
