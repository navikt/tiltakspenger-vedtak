package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.inn

import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

data class PeriodeMedYtelse(
    val periode: PeriodeDTO,
    val harYtelse: Boolean,
)
