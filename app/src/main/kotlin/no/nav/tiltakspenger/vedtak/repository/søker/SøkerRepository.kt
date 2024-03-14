package no.nav.tiltakspenger.vedtak.repository.søker

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.innsending.Søker

interface SøkerRepository {
    fun findByIdent(ident: String): Søker?
    fun hent(søkerId: SøkerId): Søker?
    fun lagre(søker: Søker)
}
