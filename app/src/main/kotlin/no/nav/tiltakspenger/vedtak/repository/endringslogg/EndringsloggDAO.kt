package no.nav.tiltakspenger.vedtak.repository.endringslogg

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.saksbehandling.domene.endringslogg.Endring
import no.nav.tiltakspenger.saksbehandling.domene.endringslogg.Endringslogg
import no.nav.tiltakspenger.saksbehandling.domene.endringslogg.Hendelsetype
import no.nav.tiltakspenger.vedtak.db.objectMapper
import no.nav.tiltakspenger.vedtak.db.readMap
import org.intellij.lang.annotations.Language

class EndringsloggDAO {

    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): List<Endring> =
        txSession.run(queryOf(hentByBehandlingId, behandlingId.toString()).map(toEndring).asList)
            .sorted()

    fun hent(sakId: SakId, txSession: TransactionalSession): List<Endring> =
        txSession.run(queryOf(hentBySakId, sakId.toString()).map(toEndring).asList)
            .sorted()

    fun lagre(sakId: SakId, behandlingId: BehandlingId?, endringslogg: Endringslogg, txSession: TransactionalSession) {
        endringslogg.endringsliste.forEach { endring ->
            lagreEndring(sakId, behandlingId, endring, txSession)
        }
    }

    private fun lagreEndring(
        sakId: SakId,
        behandlingId: BehandlingId?,
        endring: Endring,
        txSession: TransactionalSession,
    ) {
        txSession.run(
            queryOf(
                lagre,
                mapOf(
                    "id" to random(ULID_PREFIX_ENDRING).toString(),
                    "sakId" to sakId.toString(),
                    "behandlingId" to behandlingId?.toString(),
                    "brukernavn" to endring.brukernavn,
                    "type" to if (endring is Endring.Hendelse) endring.type.name else null,
                    "alvorlighetsgrad" to endring.alvorlighetsgrad,
                    "label" to endring.label,
                    "melding" to endring.melding,
                    "tidsstempel" to endring.tidsstempel,
                    "detaljer" to if (endring is Endring.Hendelse) {
                        objectMapper.writeValueAsString(
                            endring.detaljer,
                        )
                    } else {
                        null
                    },
                    "kontekster" to objectMapper.writeValueAsString(endring.kontekster),
                ),
            ).asUpdate,
        )
    }

    private val toEndring: (Row) -> Endring = { row ->
        val label = row.string("label")
        val brukernavn = row.stringOrNull("brukernavn")
        val melding = row.string("melding")
        val tidsstempl = row.localDateTime("tidsstempel")
        val detaljer: Map<String, Any> = row.stringOrNull("detaljer")
            ?.let { objectMapper.readMap(it) } ?: emptyMap()
        val kontekster: Map<String, String> = row.string("kontekster")
            .let { objectMapper.readMap(it) }

        when (label) {
            "I" -> Endring.Info(
                kontekster = kontekster,
                brukernavn = brukernavn,
                melding = melding,
                tidsstempel = tidsstempl,
            )

            "H" -> Endring.Hendelse(
                type = row.string("type")
                    .let { Hendelsetype.valueOf(it) },
                kontekster = kontekster,
                brukernavn = brukernavn,
                melding = melding,
                detaljer = detaljer,
                tidsstempel = tidsstempl,
            )

            else -> throw IllegalStateException("Ukjent Labeltype")
        }
    }

    @Language("SQL")
    private val lagre = """
            insert into endring (
            id, 
            sak_id,
            behandling_id,
            brukernavn,
            type,
            alvorlighetsgrad, 
            label, 
            melding, 
            tidsstempel,
            detaljer,
            kontekster
            ) values (
            :id, 
            :sakId,
            :behandlingId,
            :brukernavn,
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
    private val hentByBehandlingId = "select * from endring where behandling_id = ?"

    @Language("SQL")
    private val hentBySakId = "select * from endring where sak_id = ?"

    companion object {
        private const val ULID_PREFIX_ENDRING = "endr"
    }
}
