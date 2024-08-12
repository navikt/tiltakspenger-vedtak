package no.nav.tiltakspenger.felles

import no.nav.tiltakspenger.libs.common.Rolle

data class Systembruker(
    override val brukernavn: String,
    override val roller: List<Rolle>,
) : Bruker
