package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

data class PeriodeMedYtelseDto(
    val periode: PeriodeDTO,
    val harYtelse: HarYtelseDto,
)

enum class HarYtelseDto {
    HAR_YTELSE,
    HAR_IKKE_YTELSE,
}
