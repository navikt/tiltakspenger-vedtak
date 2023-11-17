package no.nav.tiltakspenger.vedtak.routes.behandling

data class BehandlingDTO(
    val id: String,
    val ident: String,
    val status: String,
    val saksbehandler: String?,
    val beslutter: String?,
)
