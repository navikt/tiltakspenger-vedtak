package no.nav.tiltakspenger.vedtak.clients.pdfgen

internal data class BrevTiltaksinfoDTO(
    val tiltak: String,
    val tiltaksnavn: String,
    val tiltaksnummer: String,
    val arrangør: String,
)
