package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.felles.ÅrsakTilEndringDTO
import java.time.LocalDateTime

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class LivsoppholdSaksopplysningDTO(
    val harLivsoppholdYtelser: Boolean,
    val årsakTilEndringLivsopphold: ÅrsakTilEndringDTO?,
    val tidspunkt: LocalDateTime,
)

internal fun LivsoppholdSaksopplysning.toDTO(): LivsoppholdSaksopplysningDTO =
    LivsoppholdSaksopplysningDTO(
        harLivsoppholdYtelser = this.harLivsoppholdYtelser,
        årsakTilEndringLivsopphold =
        when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        tidspunkt = tidsstempel,
    )
