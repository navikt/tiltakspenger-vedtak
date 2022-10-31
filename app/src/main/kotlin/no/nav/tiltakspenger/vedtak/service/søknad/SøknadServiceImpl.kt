package no.nav.tiltakspenger.vedtak.service.søknad

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository

class SøknadServiceImpl(
    private val søkerRepository: SøkerRepository,
    private val behandlingMapper: BehandlingMapper = BehandlingMapper()
) : SøknadService {

    override fun hentBehandlingAvSøknad(søknadId: String, saksbehandler: Saksbehandler): BehandlingDTO? =
        søkerRepository.findBySøknadId(søknadId)?.let {
            it.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
            behandlingMapper.mapSøkerMedSøknad(it, søknadId)
        }
}
