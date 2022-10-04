package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import org.intellij.lang.annotations.Language
import java.util.*

class AktivitetsloggDAO {

    val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    private val toAktivitet: (Row) -> Aktivitetslogg.Aktivitet = { row ->
        val label = row.string("label")

        when (label) {
            "I" -> Aktivitetslogg.Aktivitet.Info(
                kontekster = listOf(),
                melding = row.string("melding"),
                tidsstempel = row.localDateTime("tidsstempel")
            )

            "W" -> Aktivitetslogg.Aktivitet.Warn(
                kontekster = listOf(),
                melding = row.string("melding"),
                tidsstempel = row.localDateTime("tidsstempel")
            )

            "E" -> Aktivitetslogg.Aktivitet.Error(
                kontekster = listOf(),
                melding = row.string("melding"),
                tidsstempel = row.localDateTime("tidsstempel")
            )

            "S" -> Aktivitetslogg.Aktivitet.Severe(
                kontekster = listOf(),
                melding = row.string("melding"),
                tidsstempel = row.localDateTime("tidsstempel")
            )

            "B" -> Aktivitetslogg.Aktivitet.Behov(
                type = row.string("behovtype").let { Aktivitetslogg.Aktivitet.Behov.Behovtype.valueOf(it) },
                kontekster = listOf(),
                melding = row.string("melding"),
                detaljer = objectMapper.readTree(row.string("detaljer")).,
                tidsstempel = row.localDateTime("tidsstempel")
            )

            else -> throw IllegalStateException("Ukjent Labeltype")
        }

    }

    @Language("SQL")
    private val lagreAktivitetslogg = """
        insert into aktivitet (
        id, 
        søker_id, 
        type,
        alvorlighetsgrad, 
        label, 
        melding, 
        tidsstempel,
        detaljer
        ) values (
        :id, 
        :sokerId, 
        :type,
        :alvorlighetsgrad, 
        :label, 
        :melding, 
        :tidsstempel,
        :detaljer
        )
    """.trimIndent()


    @Language("SQL")
    private val slettAktiviteter = "delete from aktivitet where søker_id = ?"

    @Language("SQL")
    private val hentAktivitetslogger =
        "select * from aktivitet where søker_id = ?"

    fun lagre(søkerId: UUID, aktivitetslogg: Aktivitetslogg, txSession: TransactionalSession) {
        slettAktiviteter(søkerId, txSession)
        aktivitetslogg.aktiviteter.forEach { aktivitet ->
            txSession.run(
                queryOf(
                    lagreAktivitetslogg, mapOf(
                        "id" to UUID.randomUUID(),
                        "sokerId" to søkerId,
                        "type" to if (aktivitet is Aktivitetslogg.Aktivitet.Behov) aktivitet.type else null,
                        "alvorlighetsgrad" to aktivitet.alvorlighetsgrad,
                        "label" to aktivitet.label,
                        "melding" to aktivitet.melding,
                        "tidsstempel" to aktivitet.tidsstempel,
                        "detaljer" to if (aktivitet is Aktivitetslogg.Aktivitet.Behov) objectMapper.writeValueAsString(
                            aktivitet.detaljer
                        ) else null,
                    )
                ).asUpdate
            )
        }
    }

    private fun slettAktiviteter(søkerId: UUID, txSession: TransactionalSession) {
        txSession.run(
            queryOf(slettAktiviteter, søkerId).asUpdate
        )
    }

    fun hent(søkerId: UUID, txSession: TransactionalSession) =
        Aktivitetslogg(
            aktiviteter = txSession.run(queryOf(hentAktivitetslogger, søkerId).map(toAktivitet).asList)
                .sorted()
                .toMutableList()
        )
}
