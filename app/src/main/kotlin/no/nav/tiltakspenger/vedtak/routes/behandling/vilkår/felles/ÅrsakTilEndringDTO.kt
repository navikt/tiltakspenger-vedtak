package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.felles

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring

/**
 * Kun til bruk i route-laget.
 */
internal enum class ÅrsakTilEndringDTO {
    FEIL_I_INNHENTET_DATA,
    ENDRING_ETTER_SØKNADSTIDSPUNKT,
    ;

    fun toDomain(): ÅrsakTilEndring {
        return when (this) {
            FEIL_I_INNHENTET_DATA -> ÅrsakTilEndring.FEIL_I_INNHENTET_DATA
            ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT
        }
    }
}

internal fun ÅrsakTilEndring.toDTO(): ÅrsakTilEndringDTO {
    return when (this) {
        ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
        ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
    }
}
