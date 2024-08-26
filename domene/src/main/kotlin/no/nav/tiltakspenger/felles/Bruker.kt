package no.nav.tiltakspenger.felles

import no.nav.tiltakspenger.libs.common.Roller

interface Bruker {
    val brukernavn: String
    val roller: Roller
}
