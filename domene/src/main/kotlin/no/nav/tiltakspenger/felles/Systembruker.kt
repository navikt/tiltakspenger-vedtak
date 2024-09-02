package no.nav.tiltakspenger.felles

import no.nav.tiltakspenger.libs.common.Roller

data class Systembruker(
    override val brukernavn: String,
    override val roller: Roller,
) : Bruker
