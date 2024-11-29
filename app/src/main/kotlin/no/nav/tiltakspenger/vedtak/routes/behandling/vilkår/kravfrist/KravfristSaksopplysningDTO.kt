package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.ÅrsakTilEndringDbType
import java.time.LocalDateTime

internal data class KravfristSaksopplysningDTO(
    val kravdato: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndringDbType?,
    val kilde: KravfristKildeDTO,
)

internal fun KravfristSaksopplysning.toDTO(kilde: KravfristKildeDTO): KravfristSaksopplysningDTO =
    KravfristSaksopplysningDTO(
        kravdato = kravdato,
        årsakTilEndring =
        when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDbType.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDbType.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        kilde = kilde,
    )
