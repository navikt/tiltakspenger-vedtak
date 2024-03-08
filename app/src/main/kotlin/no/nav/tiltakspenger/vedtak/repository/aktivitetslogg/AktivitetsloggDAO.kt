package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.IAktivitetslogg
import no.nav.tiltakspenger.innsending.Kontekst
import no.nav.tiltakspenger.vedtak.db.deserializeList
import no.nav.tiltakspenger.vedtak.db.objectMapper
import no.nav.tiltakspenger.vedtak.db.readMap
import no.nav.tiltakspenger.vedtak.db.serialize
import org.intellij.lang.annotations.Language
import java.util.*

class AktivitetsloggDAO {

    private val toAktivitet: (Row) -> no.nav.tiltakspenger.innsending.Aktivitetslogg.Aktivitet = { row ->
        val label = row.string("label")
        val melding = row.string("melding")
        val tidsstempl = row.localDateTime("tidsstempel")
        val detaljer: Map<String, Any> = row.stringOrNull("detaljer")
            ?.let { objectMapper.readMap(it) } ?: emptyMap()
        val konteksterString = row.string("kontekster")
        val kontekster: List<no.nav.tiltakspenger.innsending.Kontekst> = deserializeList(konteksterString)

        when (label) {
            "I" -> no.nav.tiltakspenger.innsending.Aktivitetslogg.Aktivitet.Info(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
                persistert = true,
            )

            "W" -> no.nav.tiltakspenger.innsending.Aktivitetslogg.Aktivitet.Warn(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
                persistert = true,
            )

            "E" -> no.nav.tiltakspenger.innsending.Aktivitetslogg.Aktivitet.Error(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
                persistert = true,
            )

            "S" -> no.nav.tiltakspenger.innsending.Aktivitetslogg.Aktivitet.Severe(
                kontekster = kontekster,
                melding = melding,
                tidsstempel = tidsstempl,
                persistert = true,
            )

            "N" -> no.nav.tiltakspenger.innsending.Aktivitetslogg.Aktivitet.Behov(
                type = row.string("type").let { no.nav.tiltakspenger.innsending.Aktivitetslogg.Aktivitet.Behov.Behovtype.valueOf(it) },
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

    fun lagre(innsendingId: InnsendingId, aktivitetslogg: no.nav.tiltakspenger.innsending.IAktivitetslogg, txSession: TransactionalSession) {
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
                            "type" to if (aktivitet is no.nav.tiltakspenger.innsending.Aktivitetslogg.Aktivitet.Behov) aktivitet.type.name else null,
                            "alvorlighetsgrad" to aktivitet.alvorlighetsgrad,
                            "label" to aktivitet.label,
                            "melding" to aktivitet.melding,
                            "tidsstempel" to aktivitet.tidsstempel,
                            "detaljer" to if (aktivitet is no.nav.tiltakspenger.innsending.Aktivitetslogg.Aktivitet.Behov) {
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
        no.nav.tiltakspenger.innsending.Aktivitetslogg(
            aktiviteter = txSession.run(queryOf(hentAktivitetslogger, innsendingId.toString()).map(toAktivitet).asList)
                .sorted()
                .toMutableList(),
        )
}
