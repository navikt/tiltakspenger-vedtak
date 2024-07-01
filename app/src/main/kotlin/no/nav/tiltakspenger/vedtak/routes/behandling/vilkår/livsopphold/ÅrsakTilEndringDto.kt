package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.AarsakTilEndring

internal enum class ÅrsakTilEndringDto {
    FEIL_I_INNHENTET_DATA,
    ENDRING_ETTER_SØKNADSTIDSPUNKT,
    ;

    fun toDomain(): AarsakTilEndring {
        return when (this) {
            FEIL_I_INNHENTET_DATA -> AarsakTilEndring.FEIL_I_INNHENTET_DATA
            ENDRING_ETTER_SØKNADSTIDSPUNKT -> AarsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT
        }
    }
}
