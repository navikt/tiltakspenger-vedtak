package no.nav.tiltakspenger.vedtak.routes.behandling

import java.time.LocalDate

data class BehandlingDTO(
    val id: String,
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val typeBehandling: String,
    val status: String,
    val saksbehandler: String?,
    val beslutter: String?,
)
