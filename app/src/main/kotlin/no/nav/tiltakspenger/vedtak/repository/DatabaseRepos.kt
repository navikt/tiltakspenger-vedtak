package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadRepository

data class DatabaseRepos (
    val søkerRepo: SøkerRepository,
    val søknadRepo: SøknadRepository
)
