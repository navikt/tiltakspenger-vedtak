package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut

import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

data class PeriodeMedYtelseDTO(
    val periode: PeriodeDTO,
    val harYtelse: HarYtelseDto,
)
