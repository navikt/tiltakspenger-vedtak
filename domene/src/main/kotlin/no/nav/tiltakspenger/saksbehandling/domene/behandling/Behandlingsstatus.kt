package no.nav.tiltakspenger.saksbehandling.domene.behandling

/**
 * Kun tenkt brukt for Førstegangsbehandling i første omgang.
 * Det kan hende den passer for Revurdering også, men vurderer det når vi kommer dit.
 */
enum class Behandlingsstatus {
    /** Det står ikke en saksbehandler på behandlingen. Kan også være underkjent dersom en saksbehandler har meldt seg av behandlignen. */
    KLAR_TIL_BEHANDLING,

    /** En saksbehandler står på behandlingen. Kan også være underkjent. */
    UNDER_BEHANDLING,

    /** Saksbehandler har sendt til beslutning, men ingen beslutter er knyttet til behandlingen enda */
    KLAR_TIL_BESLUTNING,

    /** En beslutter har tatt behandlingen. */
    UNDER_BESLUTNING,

    INNVILGET,
}
