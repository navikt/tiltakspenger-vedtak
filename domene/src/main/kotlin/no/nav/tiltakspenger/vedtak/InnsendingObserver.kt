package no.nav.tiltakspenger.vedtak


import com.fasterxml.jackson.databind.JsonNode
import java.time.Duration
import java.time.LocalDateTime

interface InnsendingObserver {
    data class InnsendingEndretTilstandEvent(
        val journalpostId: String,
        val gjeldendeTilstand: InnsendingTilstandType,
        val forrigeTilstand: InnsendingTilstandType,
        val aktivitetslogg: Aktivitetslogg,
        val timeout: Duration
    )

    enum class Type {
        NySøknad,
        Gjenopptak,
        Utdanning,
        Etablering,
        KlageOgAnke,
        KlageOgAnkeLønnskompensasjon,
        Ettersending,
        UkjentSkjemaKode,
        UtenBruker,
        KlageOgAnkeForskudd,
        KlageOgAnkeFeriepenger
    }

    data class InnsendingEvent(
        val type: Type,
        val skjemaKode: String,
        val journalpostId: String,
        val aktørId: String?,
        val fødselsnummer: String?,
        val fagsakId: String?,
        val datoRegistrert: LocalDateTime,
        val søknadsData: JsonNode?,
        val behandlendeEnhet: String,
        val oppfyllerMinsteinntektArbeidsinntekt: Boolean?,
        val tittel: String,
    )

    fun tilstandEndret(event: InnsendingEndretTilstandEvent) {}
    fun innsendingFerdigstilt(event: InnsendingEvent) {}
    fun innsendingMottatt(event: InnsendingEvent) {}
}
