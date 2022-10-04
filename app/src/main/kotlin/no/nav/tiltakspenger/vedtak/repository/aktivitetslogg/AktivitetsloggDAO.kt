package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Kontekst
import no.nav.tiltakspenger.vedtak.db.deserializeList
import no.nav.tiltakspenger.vedtak.db.objectMapper
import no.nav.tiltakspenger.vedtak.db.readMap
import org.intellij.lang.annotations.Language
import java.util.*

class AktivitetsloggDAO {

    private val toAktivitet: (Row) -> Aktivitetslogg.Aktivitet = { row ->
        val label = row.string("label")
        val melding = row.string("melding")
        val tidsstempl = row.localDateTime("tidsstempel")
        val detaljer: Map<String, Any> = row.stringOrNull("detaljer")
            ?.let { objectMapper.readMap(it) } ?: emptyMap()
        val kontekster: List<Kontekst> = deserializeList(row.string("kontekster"))

        when (label) {
            "I" -> Aktivitetslogg.Aktivitet.Info(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
            )

            "W" -> Aktivitetslogg.Aktivitet.Warn(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
            )

            "E" -> Aktivitetslogg.Aktivitet.Error(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
            )

            "S" -> Aktivitetslogg.Aktivitet.Severe(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
            )

            "N" -> Aktivitetslogg.Aktivitet.Behov(
                type = row.string("type").let { Aktivitetslogg.Aktivitet.Behov.Behovtype.valueOf(it) },
                kontekster = kontekster,
                melding = melding,
                detaljer = detaljer,
                tidsstempel = tidsstempl,
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
        detaljer,
        kontekster
        ) values (
        :id, 
        :sokerId, 
        :type,
        :alvorlighetsgrad, 
        :label, 
        :melding, 
        :tidsstempel,
        to_json(:detaljer::json),
        to_json(:kontekster::json)
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
                        "type" to if (aktivitet is Aktivitetslogg.Aktivitet.Behov) aktivitet.type.name else null,
                        "alvorlighetsgrad" to aktivitet.alvorlighetsgrad,
                        "label" to aktivitet.label,
                        "melding" to aktivitet.melding,
                        "tidsstempel" to aktivitet.tidsstempel,
                        "detaljer" to if (aktivitet is Aktivitetslogg.Aktivitet.Behov) objectMapper.writeValueAsString(
                            aktivitet.detaljer
                        ) else null,
                        "kontekster" to objectMapper.writeValueAsString(aktivitet.kontekster)
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
