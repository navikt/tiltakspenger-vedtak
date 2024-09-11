package no.nav.tiltakspenger.vedtak.routes.meldekort.dto

import no.nav.tiltakspenger.utbetaling.domene.Sats
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

data class SatsDTO(
    val periode: PeriodeDTO,
    val sats: Int,
    val satsDelvis: Int,
)

fun Sats.toDTO(): SatsDTO = SatsDTO(periode = periode.toDTO(), sats = sats, satsDelvis = satsRedusert)
