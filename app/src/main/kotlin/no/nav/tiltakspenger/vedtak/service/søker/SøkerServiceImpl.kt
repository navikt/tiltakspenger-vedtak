package no.nav.tiltakspenger.vedtak.service.søker

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository

class SøkerServiceImpl(
    val søkerRepository: SøkerRepository,
    private val behandlingMapper: BehandlingMapper = BehandlingMapper()
) : SøkerService {

    override fun hentSøkerOgSøknader(ident: String, saksbehandler: Saksbehandler): SøkerDTO? {
        val søker = søkerRepository.hent(ident) ?: return null
        søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)

        return SøkerDTO(
            ident = søker.ident,
            behandlinger = behandlingMapper.mapSøkerMedSøknad(søker)
        )
    }
}
