package no.nav.tiltakspenger.vedtak.repository.foreldrepenger

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.ForeldrepengerVedtakId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.innsending.domene.ForeldrepengerVedtak
import org.intellij.lang.annotations.Language

class ForeldrepengerAnvisningDAO() {
    fun hentForForeldrepengerVedtak(
        foreldrepengerVedtakId: ForeldrepengerVedtakId,
        txSession: TransactionalSession,
    ): List<ForeldrepengerVedtak.ForeldrepengerAnvisning> {
        return txSession.run(
            queryOf(hentForeldrepengerAnvisning, foreldrepengerVedtakId.toString())
                .map { row -> row.toForeldrepengerAnvisning() }
                .asList,
        )
    }

    fun lagre(
        foreldrepengerVedtakId: ForeldrepengerVedtakId,
        anvisninger: List<ForeldrepengerVedtak.ForeldrepengerAnvisning>,
        txSession: TransactionalSession,
    ) {
        slettForeldrepengerAnvisninger(foreldrepengerVedtakId, txSession)
        anvisninger.forEach { anvisning ->
            lagreForeldrepengerAnvisning(foreldrepengerVedtakId, anvisning, txSession)
        }
    }

    private fun lagreForeldrepengerAnvisning(
        foreldrepengerVedtakId: ForeldrepengerVedtakId,
        anvisning: ForeldrepengerVedtak.ForeldrepengerAnvisning,
        txSession: TransactionalSession,
    ) {
        val id = random(ULID_PREFIX_ANVISNING)
        txSession.run(
            queryOf(
                lagreForeldrepengerAnvisning,
                mapOf(
                    "id" to id.toString(),
                    "foreldrepengerVedtakId" to foreldrepengerVedtakId.toString(),
                    "fra" to anvisning.periode.fra,
                    "til" to anvisning.periode.til,
                    "belop" to anvisning.beløp,
                    "dagsats" to anvisning.dagsats,
                    "utbetalingsgrad" to anvisning.utbetalingsgrad,
                ),
            ).asUpdate,
        )
    }

    fun slettForeldrepengerAnvisninger(
        foreldrepengerVedtakId: ForeldrepengerVedtakId,
        txSession: TransactionalSession,
    ) {
        txSession.run(queryOf(slettForeldrepengerAnvisning, foreldrepengerVedtakId.toString()).asUpdate)
    }

    private fun Row.toForeldrepengerAnvisning(): ForeldrepengerVedtak.ForeldrepengerAnvisning {
        return ForeldrepengerVedtak.ForeldrepengerAnvisning(
            periode = Periode(fra = localDate("fra"), til = localDate("til")),
            beløp = bigDecimalOrNull("beløp"),
            dagsats = bigDecimalOrNull("dagsats"),
            utbetalingsgrad = bigDecimalOrNull("utbetalingsgrad"),
        )
    }

    @Language("SQL")
    private val lagreForeldrepengerAnvisning = """
        insert into foreldrepenger_anvisning (
            id,
            foreldrepenger_vedtak_id,
            fra,
            til,
            beløp,
            dagsats,
            utbetalingsgrad
        ) values (
            :id, 
            :foreldrepengerVedtakId,
            :fra,
            :til,
            :belop,
            :dagsats,
            :utbetalingsgrad
        )
    """.trimIndent()

    @Language("SQL")
    private val slettForeldrepengerAnvisning = """
        delete from foreldrepenger_anvisning 
        where foreldrepenger_vedtak_id in (select id from foreldrepenger_vedtak where id = ?)
    """.trimIndent()

    @Language("SQL")
    private val hentForeldrepengerAnvisning =
        "select * from foreldrepenger_anvisning where foreldrepenger_vedtak_id = ?"

    companion object {
        private const val ULID_PREFIX_ANVISNING = "fpan"
    }
}
