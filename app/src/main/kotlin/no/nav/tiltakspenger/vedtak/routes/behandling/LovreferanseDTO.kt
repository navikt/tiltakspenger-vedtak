package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse

data class LovreferanseDTO(
    val lovverk: String,
    val paragraf: String,
    val beskrivelse: String,
)

internal fun Lovreferanse.toDTO() =
    LovreferanseDTO(
        lovverk = this.lovverk,
        paragraf = this.paragraf,
        beskrivelse = this.beskrivelse,
    )
