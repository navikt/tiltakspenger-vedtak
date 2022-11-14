package no.nav.tiltakspenger.vedtak.service.søknad

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository

class SøknadServiceImpl(
    private val innsendingRepository: InnsendingRepository,
    private val behandlingMapper: BehandlingMapper = BehandlingMapper()
) : SøknadService {

    override fun hentBehandlingAvSøknad(søknadId: String, saksbehandler: Saksbehandler): BehandlingDTO? =
        innsendingRepository.findBySøknadId(søknadId)?.let {
            it.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
            behandlingMapper.mapInnsendingMedSøknad(it)
        }
}
