package no.nav.tiltakspenger.domene

import java.util.UUID

data class Sak(
    val id: UUID,
    val saknummer: String,
    val behandlinger: List<Behandling>,
)
