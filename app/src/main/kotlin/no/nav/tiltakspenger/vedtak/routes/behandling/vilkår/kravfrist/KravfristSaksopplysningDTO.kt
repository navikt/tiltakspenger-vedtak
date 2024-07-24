package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristSaksopplysning
import java.time.LocalDateTime

internal data class KravfristSaksopplysningDTO(
    val kravdato: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val kilde: KravfristKildeDTO,
) {
    enum class ÅrsakTilEndringDTO {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
    }
}

internal fun KravfristSaksopplysning.toDTO(kilde: KravfristKildeDTO): KravfristSaksopplysningDTO {
    return KravfristSaksopplysningDTO(
        kravdato = kravdato,
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> KravfristSaksopplysningDTO.ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> KravfristSaksopplysningDTO.ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        kilde = kilde,
    )
}
