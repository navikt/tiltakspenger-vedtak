package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring

internal enum class KravfristÅrsakTilEndringDTO {
    FEIL_I_INNHENTET_DATA,
    ENDRING_ETTER_SØKNADSTIDSPUNKT,
    ;

    fun toDomain(): ÅrsakTilEndring =
        when (this) {
            FEIL_I_INNHENTET_DATA -> ÅrsakTilEndring.FEIL_I_INNHENTET_DATA
            ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT
        }
}
