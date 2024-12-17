package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus

enum class BehandlingsstatusDTO {
    KLAR_TIL_BEHANDLING,
    UNDER_BEHANDLING,
    KLAR_TIL_BESLUTNING,
    UNDER_BESLUTNING,

    // TODO behandlingsstatus jah: Endre denne til VEDATT her, samtidig med tiltakspenger-saksbehandling (frontend)
    INNVILGET,
}

fun Behandlingsstatus.toDTO(): BehandlingsstatusDTO =
    when (this) {
        Behandlingsstatus.KLAR_TIL_BEHANDLING -> BehandlingsstatusDTO.KLAR_TIL_BEHANDLING
        Behandlingsstatus.UNDER_BEHANDLING -> BehandlingsstatusDTO.UNDER_BEHANDLING
        Behandlingsstatus.KLAR_TIL_BESLUTNING -> BehandlingsstatusDTO.KLAR_TIL_BESLUTNING
        Behandlingsstatus.UNDER_BESLUTNING -> BehandlingsstatusDTO.UNDER_BESLUTNING
        Behandlingsstatus.VEDTATT -> BehandlingsstatusDTO.INNVILGET
    }
