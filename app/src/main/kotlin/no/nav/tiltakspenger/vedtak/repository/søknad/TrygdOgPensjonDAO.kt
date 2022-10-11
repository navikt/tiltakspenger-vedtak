package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import org.intellij.lang.annotations.Language
import java.util.*

internal class TrygdOgPensjonDAO {

    fun lagre(søknadId: UUID, trygdOgPensjon: List<TrygdOgPensjon>?, txSession: TransactionalSession) {
        slettTrygdOgPensjon(søknadId, txSession)
        trygdOgPensjon?.forEach {
            lagreTrygdOgPensjon(søknadId, it, txSession)
        }
    }

    fun hentTrygdOgPensjonListe(søknadId: UUID, txSession: TransactionalSession): List<TrygdOgPensjon> {
        return txSession.run(
            queryOf(hentTrygdOgPensjon, søknadId)
                .map { row ->
                    row.toTrygdOgPensjon()
                }.asList
        )
    }

    private fun lagreTrygdOgPensjon(søknadId: UUID, trygdOgPensjon: TrygdOgPensjon, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                lagreTrygdOgPensjon, mapOf(
                    "id" to UUID.randomUUID(),
                    "soknadId" to søknadId,
                    "utbetaler" to trygdOgPensjon.utbetaler,
                    "prosent" to trygdOgPensjon.prosent,
                    "fom" to trygdOgPensjon.fom,
                    "tom" to trygdOgPensjon.tom,
                )
            ).asUpdate
        )
    }

    private fun slettTrygdOgPensjon(søknadId: UUID, txSession: TransactionalSession) {
        txSession.run(
            queryOf(slettTrygdOgPensjon, søknadId).asUpdate
        )
    }

    private fun Row.toTrygdOgPensjon(): TrygdOgPensjon {
        val utbetaler = string("utbetaler")
        val prosent = intOrNull("prosent")
        val fom = localDateOrNull("fom")
        val tom = localDateOrNull("tom")

        return TrygdOgPensjon(
            utbetaler = utbetaler,
            prosent = prosent,
            fom = fom,
            tom = tom,
        )
    }

    @Language("SQL")
    private val hentTrygdOgPensjon = "select * from søknad_trygdogpensjon where søknad_id = ?"

    @Language("SQL")
    private val slettTrygdOgPensjon = "delete from søknad_trygdogpensjon where søknad_id = ?"

    @Language("SQL")
    private val lagreTrygdOgPensjon = """
        insert into søknad_trygdogpensjon (
            id,
            søknad_id,
            utbetaler,
            prosent,
            fom,
            tom
        ) values (
            :id,
            :soknadId,
            :utbetaler,
            :prosent,
            :fom,
            :tom
        )""".trimIndent()
}
