package no.nav.tiltakspenger.vedtak


import java.time.Duration

interface SøkerObserver {
    data class SøkerEndretTilstandEvent(
        val ident: String,
        val gjeldendeTilstand: SøkerTilstandType,
        val forrigeTilstand: SøkerTilstandType,
        val aktivitetslogg: Aktivitetslogg,
        val timeout: Duration
    )

    fun tilstandEndret(event: SøkerEndretTilstandEvent) {}
}
