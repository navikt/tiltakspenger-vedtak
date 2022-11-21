package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.Søker

interface SøkerRepository {
    fun hent(ident: String): Søker?
    fun hentBySøkerId(søkerId: SøkerId): Søker?
    fun lagre(søker: Søker)
    fun findBySøknadId(søknadId: String): Søker?
}
