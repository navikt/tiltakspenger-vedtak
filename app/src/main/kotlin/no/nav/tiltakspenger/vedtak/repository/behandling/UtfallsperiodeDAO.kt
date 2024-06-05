package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsdetaljer
import org.intellij.lang.annotations.Language

class UtfallsperiodeDAO {

    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): Periodisering<Utfallsdetaljer> {
        val liste: List<PeriodeMedVerdi<Utfallsdetaljer>> = txSession.run(
            queryOf(hentUtfallsperioder, behandlingId.toString())
                .map { row -> row.toUtfallsperiode() }
                .asList,
        )
        return Periodisering.fraPeriodeListe(liste)
    }

    fun hentForVedtak(vedtakId: VedtakId, txSession: TransactionalSession): Periodisering<Utfallsdetaljer> {
        val liste = txSession.run(
            queryOf(hentUtfallsperioderForVedtak, vedtakId.toString())
                .map { row -> row.toUtfallsperiode() }
                .asList,
        )
        return Periodisering.fraPeriodeListe(liste)
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

    fun lagre(
        behandlingId: BehandlingId,
        utfallsperioder: Periodisering<Utfallsdetaljer>,
        txSession: TransactionalSession,
    ) {
        slettUtfallsperioder(behandlingId, txSession)
        utfallsperioder.perioder().forEach { utfallsperiode ->
            lagreUtfallsperiode(behandlingId, utfallsperiode, txSession)
        }
    }

    private fun lagreUtfallsperiode(
        behandlingId: BehandlingId,
        utfallsperiode: PeriodeMedVerdi<Utfallsdetaljer>,
        txSession: TransactionalSession,
    ) {
        txSession.run(
            queryOf(
                lagreUtfallsperiode,
                mapOf(
                    "id" to random(ULID_PREFIX_UTFALLSPERIODE).toString(),
                    "behandlingId" to behandlingId.toString(),
                    "fom" to utfallsperiode.periode.fra,
                    "tom" to utfallsperiode.periode.til,
                    "antallBarn" to utfallsperiode.verdi.antallBarn,
                    "utfall" to utfallsperiode.verdi.utfall.name,
                ),
            ).asUpdate,
        )
    }

    private fun slettUtfallsperioder(behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettUtfallsperioder, behandlingId.toString()).asUpdate)
    }

    private fun Row.toUtfallsperiode(): PeriodeMedVerdi<Utfallsdetaljer> {
        return PeriodeMedVerdi(
            verdi = Utfallsdetaljer(
                antallBarn = int("antall_barn"),
                utfall = UtfallForPeriode.valueOf(string("utfall")),
            ),
            periode = Periode(
                fra = localDate("fom"),
                til = localDate("tom"),
            ),
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
