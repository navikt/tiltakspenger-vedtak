package no.nav.tiltakspenger.innsending

import java.time.Duration

interface InnsendingObserver {
    data class InnendingEndretTilstandEvent(
        val journalpostId: String,
        val gjeldendeTilstand: InnsendingTilstandType,
        val forrigeTilstand: InnsendingTilstandType,
        val aktivitetslogg: Aktivitetslogg,
        val timeout: Duration,
    )

    fun tilstandEndret(event: InnendingEndretTilstandEvent) {}
}
