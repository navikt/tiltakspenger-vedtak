package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

import no.nav.tiltakspenger.saksbehandling.service.søker.PeriodeDTO

data class AntallDagerDTO(
    val periode: PeriodeDTO,
    val antallDager: Int,
    val kilde: String,
)
