package no.nav.tiltakspenger.vedtak.service.søknad

import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository

class SøknadServiceImpl(
    private val søkerRepository: SøkerRepository,
    private val behandlingMapper: BehandlingMapper = BehandlingMapper()
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


    override fun hentBehandlingAvSøknad(søknadId: String): BehandlingDTO? =
        søkerRepository.findBySøknadId(søknadId)?.let { behandlingMapper.mapSøkerMedSøknad(it, søknadId) }

}
