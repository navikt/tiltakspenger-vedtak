package no.nav.tiltakspenger.vedtak.meldinger.serder

import com.fasterxml.jackson.databind.JsonNode
import no.nav.tiltakspenger.vedtak.Innsending
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

data class InnsendingData(
    val id: Long,
    val journalpostId: String,
    val tilstand: TilstandData,
    val oppfyllerMinsteArbeidsinntekt: Boolean?,
    val eksisterendeSaker: Boolean?,
    val personData: PersonData?,
    val arenaSakData: ArenaSakData?,
    val søknadsData: JsonNode?,
    val aktivitetslogg: AktivitetsloggData
) {
    fun createInnsending(): Innsending {
        return Innsending::class.primaryConstructor!!
            .apply { isAccessible = true }
            .call(
                journalpostId,
                tilstand.createTilstand(),

                oppfyllerMinsteArbeidsinntekt,
                eksisterendeSaker,
                aktivitetslogg.let(::konverterTilAktivitetslogg)
            )
    }

    data class TilstandData(
        val type: InnsendingTilstandTypeData
    ) {
        fun createTilstand(): Innsending.Tilstand = when (type) {
            InnsendingTilstandTypeData.MottattType -> Innsending.Mottatt
            InnsendingTilstandTypeData.AvventerJournalpostType -> Innsending.AvventerJournalpost
        }

        enum class InnsendingTilstandTypeData {
            MottattType,
            AvventerJournalpostType
        }
    }

    data class AktivitetsloggData(
        val aktiviteter: List<AktivitetData>
    ) {
        data class AktivitetData(
            val alvorlighetsgrad: Alvorlighetsgrad,
            val label: Char,
            val behovtype: String?,
            val melding: String,
            val tidsstempel: String,
            val kontekster: List<SpesifikkKontekstData>,
            val detaljer: Map<String, Any>
        )

        data class SpesifikkKontekstData(
            val kontekstType: String,
            val kontekstMap: Map<String, String>
        )

        enum class Alvorlighetsgrad {
            INFO,
            WARN,
            BEHOV,
            ERROR,
            SEVERE
        }
    }

    data class ArenaSakData(
        val oppgaveId: String,
        val fagsakId: String?
    )

    data class PersonData(
        val navn: String,
        val aktørId: String,
        val fødselsnummer: String,
        val norskTilknytning: Boolean,
        val diskresjonskode: Boolean
    )
}