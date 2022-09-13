package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Row
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.db.DataSource.session
import org.intellij.lang.annotations.Language
import java.util.*

internal class TrygdOgPensjonRepo {

    fun lagre(søknadId: UUID, trygdOgPensjon: List<TrygdOgPensjon>?) {
        slettTrygdOgPensjon(søknadId)
        trygdOgPensjon?.forEach {
            lagreTrygdOgPensjon(søknadId, it)
        }
    }

    fun hentTrygdOgPensjonListe(søknadId: UUID): List<TrygdOgPensjon> {
        return session.run(
            queryOf(hentTrygdOgPensjon, søknadId)
                .map { row ->
                    row.toTrygdOgPensjon()
                }.asList
        )
    }

    private fun lagreTrygdOgPensjon(søknadId: UUID, trygdOgPensjon: TrygdOgPensjon) {
        session.run(
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

    private fun slettTrygdOgPensjon(søknadId: UUID): Unit {
        session.run(
            queryOf(slettTrygdOgPensjon, søknadId).asUpdate
        )
    }

    private fun Row.toTrygdOgPensjon() : TrygdOgPensjon {
        val utbetaler = string("utbetaler")
        val prosent = intOrNull("prosent")
        val fom = localDate("fom")
        val tom = localDateOrNull("tom")

        return TrygdOgPensjon(
            utbetaler = utbetaler,
            prosent = prosent,
            fom = fom,
            tom = tom,
        )
    }

    @Language("SQL")
    private val hentTrygdOgPensjon = "select * from trygdogpensjon where søknad_id = ?"

    @Language("SQL")
    private val slettTrygdOgPensjon = "delete from trygdogpensjon where søknad_id = ?"

    @Language("SQL")
    private val lagreTrygdOgPensjon = """
        insert into trygdogpensjon (
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