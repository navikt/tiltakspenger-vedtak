package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker

interface SøkerRepository {
    fun findByIdent(ident: String): Søker?
    fun hent(søkerId: SøkerId): Søker?
    fun lagre(søker: Søker)
}
