package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.vedtak.routes.dto.SaksbehandlerDTO
import java.time.LocalDateTime

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class LivsoppholdSaksopplysningDTO(
    val avklartHarLivsoppholdYtelser: Boolean,
    val periode: Periode,
    val saksbehandlerDTO: SaksbehandlerDTO?,
    val årsakTilEndringLivsoppholdDTO: ÅrsakTilEndringLivsoppholdDTO?,
    val tidspunkt: LocalDateTime,
)

internal fun LivsoppholdSaksopplysning.toDTO(): LivsoppholdSaksopplysningDTO {


}
