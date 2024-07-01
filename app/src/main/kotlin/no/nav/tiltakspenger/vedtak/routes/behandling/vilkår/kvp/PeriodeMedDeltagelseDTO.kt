package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.Deltagelse
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

data class PeriodeMedDeltagelseDTO(
    val periode: PeriodeDTO,
    val deltagelse: DeltagelseDTO,
)

enum class DeltagelseDTO {
    DELTAR,
    DELTAR_IKKE,
}

fun Deltagelse.toDTO(): DeltagelseDTO {
    return DeltagelseDTO.valueOf(this.name)
}

fun PeriodeMedVerdi<Deltagelse>.toDTO(): PeriodeMedDeltagelseDTO {
    return PeriodeMedDeltagelseDTO(
        periode = this.periode.toDTO(),
        deltagelse = this.verdi.toDTO(),
    )
}
