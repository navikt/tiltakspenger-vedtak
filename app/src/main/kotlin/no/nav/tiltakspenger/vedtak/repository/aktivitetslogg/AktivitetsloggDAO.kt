package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.IAktivitetslogg
import no.nav.tiltakspenger.vedtak.Kontekst
import no.nav.tiltakspenger.vedtak.db.deserializeList
import no.nav.tiltakspenger.vedtak.db.objectMapper
import no.nav.tiltakspenger.vedtak.db.readMap
import no.nav.tiltakspenger.vedtak.db.serialize
import org.intellij.lang.annotations.Language
import java.util.*

class AktivitetsloggDAO {

    private val toAktivitet: (Row) -> Aktivitetslogg.Aktivitet = { row ->
        val label = row.string("label")
        val melding = row.string("melding")
        val tidsstempl = row.localDateTime("tidsstempel")
        val detaljer: Map<String, Any> = row.stringOrNull("detaljer")
            ?.let { objectMapper.readMap(it) } ?: emptyMap()
        val konteksterString = row.string("kontekster")
        val kontekster: List<Kontekst> = deserializeList(konteksterString)

        when (label) {
            "I" -> Aktivitetslogg.Aktivitet.Info(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
                persistert = true,
            )

            "W" -> Aktivitetslogg.Aktivitet.Warn(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
                persistert = true,
            )

            "E" -> Aktivitetslogg.Aktivitet.Error(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
                persistert = true,
            )

            "S" -> Aktivitetslogg.Aktivitet.Severe(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
                persistert = true,
            )

            "N" -> Aktivitetslogg.Aktivitet.Behov(
                type = row.string("type").let { Aktivitetslogg.Aktivitet.Behov.Behovtype.valueOf(it) },
                kontekster = kontekster,
                melding = melding,
                detaljer = detaljer,
                tidsstempel = tidsstempl,
                persistert = true,
            )

            else -> throw IllegalStateException("Ukjent Labeltype")
        }
    }

    @Language("SQL")
    private val lagreAktivitetslogg = """
        insert into aktivitet (
        id, 
        innsending_id, 
        type,
        alvorlighetsgrad, 
        label, 
        melding, 
        tidsstempel,
        detaljer,
        kontekster
        ) values (
        :id, 
        :innsendingId, 
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
    private val slettAktiviteter = "delete from aktivitet where innsending_id = ?"

    @Language("SQL")
    private val hentAktivitetslogger = "select * from aktivitet where innsending_id = ?"

    fun lagre(innsendingId: InnsendingId, aktivitetslogg: IAktivitetslogg, txSession: TransactionalSession) {
        // slettAktiviteter(innsendingId, txSession)
        aktivitetslogg.aktiviteter()
            .filter { !it.persistert }
            .forEach { aktivitet ->
                txSession.run(
                    queryOf(
                        lagreAktivitetslogg,
                        mapOf(
                            "id" to UUID.randomUUID(),
                            "innsendingId" to innsendingId.toString(),
                            "type" to if (aktivitet is Aktivitetslogg.Aktivitet.Behov) aktivitet.type.name else null,
                            "alvorlighetsgrad" to aktivitet.alvorlighetsgrad,
                            "label" to aktivitet.label,
                            "melding" to aktivitet.melding,
                            "tidsstempel" to aktivitet.tidsstempel,
                            "detaljer" to if (aktivitet is Aktivitetslogg.Aktivitet.Behov) {
                                objectMapper.writeValueAsString(
                                    aktivitet.detaljer,
                                )
                            } else {
                                null
                            },
                            "kontekster" to aktivitet.kontekster.serialize(),
                        ),
                    ).asUpdate,
                )
            }
    }

    private fun slettAktiviteter(innsendingId: InnsendingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettAktiviteter, innsendingId.toString()).asUpdate)
    }

    fun hent(innsendingId: InnsendingId, txSession: TransactionalSession) =
        Aktivitetslogg(
            aktiviteter = txSession.run(queryOf(hentAktivitetslogger, innsendingId.toString()).map(toAktivitet).asList)
                .sorted()
                .toMutableList(),
        )
}
