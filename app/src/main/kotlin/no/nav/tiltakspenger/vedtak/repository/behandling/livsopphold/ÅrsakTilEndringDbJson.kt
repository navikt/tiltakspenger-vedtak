package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.ÅrsakTilEndring
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.ÅrsakTilEndringDbJson.ENDRING_ETTER_SØKNADSTIDSPUNKT
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.ÅrsakTilEndringDbJson.FEIL_I_INNHENTET_DATA

enum class ÅrsakTilEndringDbJson {
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

fun ÅrsakTilEndring.toDbJson(): ÅrsakTilEndringDbJson {
    return when (this) {
        ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> FEIL_I_INNHENTET_DATA
        ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ENDRING_ETTER_SØKNADSTIDSPUNKT
    }
}
