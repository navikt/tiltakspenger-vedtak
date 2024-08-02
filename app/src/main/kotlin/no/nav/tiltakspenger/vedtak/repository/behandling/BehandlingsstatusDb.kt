package no.nav.tiltakspenger.vedtak.repository.behandling

import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingsstatusDb.INNVILGET
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingsstatusDb.KLAR_TIL_BEHANDLING
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingsstatusDb.KLAR_TIL_BESLUTNING
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingsstatusDb.UNDER_BEHANDLING
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingsstatusDb.UNDER_BESLUTNING

/**
 * @see no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus
 */
private enum class BehandlingsstatusDb {
    KLAR_TIL_BEHANDLING, UNDER_BEHANDLING, KLAR_TIL_BESLUTNING, UNDER_BESLUTNING, INNVILGET
}

fun String.toBehandlingsstatus(): no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus {
    return when (BehandlingsstatusDb.valueOf(this)) {
        KLAR_TIL_BEHANDLING -> no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.KLAR_TIL_BEHANDLING
        UNDER_BEHANDLING -> no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.UNDER_BEHANDLING
        KLAR_TIL_BESLUTNING -> no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.KLAR_TIL_BESLUTNING
        UNDER_BESLUTNING -> no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.UNDER_BESLUTNING
        INNVILGET -> no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.INNVILGET
    }
}

fun no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.toDb(): String {
    return when (this) {
        no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.KLAR_TIL_BEHANDLING -> BehandlingsstatusDb.KLAR_TIL_BEHANDLING
        no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.UNDER_BEHANDLING -> BehandlingsstatusDb.UNDER_BEHANDLING
        no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.KLAR_TIL_BESLUTNING -> BehandlingsstatusDb.KLAR_TIL_BESLUTNING
        no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.UNDER_BESLUTNING -> BehandlingsstatusDb.UNDER_BESLUTNING
        no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus.INNVILGET -> BehandlingsstatusDb.INNVILGET
    }.toString()
}
