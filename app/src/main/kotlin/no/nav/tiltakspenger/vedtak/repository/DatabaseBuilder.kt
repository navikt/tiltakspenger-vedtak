package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.PostgresSøknadRepository

object DatabaseBuilder {
    fun build(): DatabaseRepos {
        val søknadRepo = PostgresSøknadRepository()
        return DatabaseRepos(
            søkerRepo = PostgresSøkerRepository(søknadRepo),
            søknadRepo = søknadRepo,
        )
    }
}
