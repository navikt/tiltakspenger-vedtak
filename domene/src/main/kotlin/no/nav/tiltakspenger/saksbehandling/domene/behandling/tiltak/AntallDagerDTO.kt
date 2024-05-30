package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

import no.nav.tiltakspenger.saksbehandling.service.søker.PeriodeDTO

data class AntallDagerDTO(
    val antallDager: Int,
    val periode: PeriodeDTO,
    val kilde: String,
)
