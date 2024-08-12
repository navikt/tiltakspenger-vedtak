package no.nav.tiltakspenger.felles

import no.nav.tiltakspenger.libs.common.Rolle

interface Bruker {
    val brukernavn: String
    val roller: List<Rolle>
}
