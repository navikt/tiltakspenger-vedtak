package no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles

/**
 * Dersom du legger til en årsak som ikke gjelder alle, må du lage egne domenetyper for dette.
 * Dersom du skal bruke denne må alle årsakene gjelde der du skal bruke den.
 */
enum class ÅrsakTilEndring {
    FEIL_I_INNHENTET_DATA,
    ENDRING_ETTER_SØKNADSTIDSPUNKT,
}
