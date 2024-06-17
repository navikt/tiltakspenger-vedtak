package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

data class AntallDagerSaksopplysningerDTO(
    val tiltak: String,
    val arrangør: String,
    val avklartAntallDager: List<AntallDagerDTO>,
    val antallDagerSaksopplysningerFraRegister: List<AntallDagerDTO>,
)
