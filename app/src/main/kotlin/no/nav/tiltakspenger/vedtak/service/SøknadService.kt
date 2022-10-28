package no.nav.tiltakspenger.vedtak.service

import no.nav.tiltakspenger.felles.SøknadId

interface SøknadService {
    fun hentSøknad(ident: String, søknadId: SøknadId): StorSøknadDTO?
}

data class StorSøknadDTO(
    val søknadId: String,
)
