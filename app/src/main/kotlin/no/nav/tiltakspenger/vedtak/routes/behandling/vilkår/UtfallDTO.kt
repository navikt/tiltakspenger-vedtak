package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDto

internal data class PeriodisertUtfallDTO(
    val utfall: UtfallDTO,
    val periode: PeriodeDTO,
) {
    enum class UtfallDTO {
        OPPFYLT,
        IKKE_OPPFYLT,
        UAVKLART,
    }
}

internal fun Periodisering<Utfall2>.toDTO(): List<PeriodisertUtfallDTO> {
    return this.perioder().map {
        PeriodisertUtfallDTO(
            utfall = it.verdi.toDTO(),
            periode = it.periode.toDto(),
        )
    }
}

internal fun Utfall2.toDTO(): PeriodisertUtfallDTO.UtfallDTO {
    return when (this) {
        Utfall2.OPPFYLT -> PeriodisertUtfallDTO.UtfallDTO.OPPFYLT
        Utfall2.IKKE_OPPFYLT -> PeriodisertUtfallDTO.UtfallDTO.IKKE_OPPFYLT
        Utfall2.UAVKLART -> PeriodisertUtfallDTO.UtfallDTO.UAVKLART
    }
}
