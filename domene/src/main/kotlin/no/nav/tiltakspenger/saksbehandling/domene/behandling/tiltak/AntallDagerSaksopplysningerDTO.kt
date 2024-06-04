package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

data class AntallDagerSaksopplysningerDTO(
    val antallDagerSaksopplysningerFraSBH: List<AntallDagerDTO>,
    val antallDagerSaksopplysningerFraRegister: List<AntallDagerDTO>,
    val avklartAntallDager: List<AntallDagerDTO>,
)
