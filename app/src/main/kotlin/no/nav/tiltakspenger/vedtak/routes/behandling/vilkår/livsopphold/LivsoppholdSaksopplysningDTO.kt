package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.DeltagelseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.PeriodeMedDeltagelseDTO
import no.nav.tiltakspenger.vedtak.routes.dto.SaksbehandlerDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO
import java.time.LocalDateTime

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class LivsoppholdSaksopplysningDTO(
    val avklartHarLivsoppholdYtelser: PeriodeMedDeltagelseDTO,
    val saksbehandlerDTO: SaksbehandlerDTO?,
    val årsakTilEndringLivsoppholdDTO: ÅrsakTilEndringDTO?,
    val tidspunkt: LocalDateTime,
) {
    enum class ÅrsakTilEndringDTO {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
    }
}

internal fun LivsoppholdSaksopplysning.toDTO(): LivsoppholdSaksopplysningDTO {
    val deltagelseDTO = when (this.harLivsoppholdYtelser) {
        true -> DeltagelseDTO.DELTAR
        false -> DeltagelseDTO.DELTAR_IKKE
    }
    return LivsoppholdSaksopplysningDTO(
        avklartHarLivsoppholdYtelser = PeriodeMedDeltagelseDTO(periode = this.periode.toDTO(), deltagelseDTO),
        saksbehandlerDTO = this.saksbehandler?.toDTO(),
        årsakTilEndringLivsoppholdDTO = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> LivsoppholdSaksopplysningDTO.ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> LivsoppholdSaksopplysningDTO.ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        tidspunkt = tidsstempel,
    )
}
