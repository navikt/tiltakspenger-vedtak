package no.nav.tiltakspenger.felles

data class Systembruker(
    override val brukernavn: String,
    override val roller: List<Rolle>,
) : Bruker
