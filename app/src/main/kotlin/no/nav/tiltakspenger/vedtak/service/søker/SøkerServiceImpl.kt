package no.nav.tiltakspenger.vedtak.service.søker

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository

class SøkerServiceImpl(
    val søkerRepository: SøkerRepository,
    val innsendingRepository: InnsendingRepository,
    private val behandlingMapper: BehandlingMapper = BehandlingMapper()
) : SøkerService {


    override fun hentSøkerId(ident: String, saksbehandler: Saksbehandler): SøkerIdDTO? {
        val søker: Søker = søkerRepository.findByIdent(ident) ?: return null
        //søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
        return SøkerIdDTO(
            id = søker.søkerId.toString(),
        )
    }

    override fun hentSøkerOgSøknader(søkerId: SøkerId, saksbehandler: Saksbehandler): SøkerDTO? {
        val søker = søkerRepository.hent(søkerId) ?: return null
        val innsendinger = innsendingRepository.findByIdent(søker.ident)
        innsendinger.forEach { it.sjekkOmSaksbehandlerHarTilgang(saksbehandler) }
        // søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
        return behandlingMapper.mapSøkerOgInnsendinger(søker, innsendinger)
    }
}
