package no.nav.tiltakspenger.vedtak


import com.fasterxml.jackson.databind.JsonNode
import java.time.Duration
import java.time.LocalDateTime

interface SøkerObserver {
    data class SøkerEndretTilstandEvent(
        val ident: String,
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

    data class SøkerEvent(
        val type: Type,
        val skjemaKode: String,
        val ident: String,
        val aktørId: String?,
        val fødselsnummer: String?,
        val fagsakId: String?,
        val datoRegistrert: LocalDateTime,
        val søknadsData: JsonNode?,
        val behandlendeEnhet: String,
        val oppfyllerMinsteinntektArbeidsinntekt: Boolean?,
        val tittel: String,
    )

    fun tilstandEndret(event: SøkerEndretTilstandEvent) {}
    fun innsendingFerdigstilt(event: SøkerEvent) {}
    fun innsendingMottatt(event: SøkerEvent) {}
}
