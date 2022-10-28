package no.nav.tiltakspenger.vedtak.service

import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository

class SøknadServiceImpl(
    val søkerRepository: SøkerRepository
) : SøknadService {
    override fun hentSøknad(ident: String, søknadId: SøknadId): StorSøknadDTO? {
        val søker = søkerRepository.hent(ident) ?: return null

        val søknad = søker.søknader.filter {
            it.id == søknadId
        }.single()

        return StorSøknadDTO(
            søknadId = søknad.id.toString()
        )
    }

}
