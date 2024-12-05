package no.nav.tiltakspenger.vedtak.repository.behandling.felles

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring

enum class ÅrsakTilEndringDbType {
    FEIL_I_INNHENTET_DATA,
    ENDRING_ETTER_SØKNADSTIDSPUNKT,
    ;

    fun toDomain(): ÅrsakTilEndring =
        when (this) {
            FEIL_I_INNHENTET_DATA -> ÅrsakTilEndring.FEIL_I_INNHENTET_DATA
            ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT
        }
}

fun ÅrsakTilEndring.toDbType(): ÅrsakTilEndringDbType {
    return when (this) {
        ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDbType.FEIL_I_INNHENTET_DATA
        ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDbType.ENDRING_ETTER_SØKNADSTIDSPUNKT
    }
}
