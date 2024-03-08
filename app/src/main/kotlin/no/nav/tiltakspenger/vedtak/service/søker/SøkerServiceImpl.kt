package no.nav.tiltakspenger.vedtak.service.søker

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.innsending.Søker
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository

private val LOG = KotlinLogging.logger {}

class SøkerServiceImpl(
    private val søkerRepository: SøkerRepository,
) : SøkerService {

    override fun hentSøkerId(ident: String, saksbehandler: Saksbehandler): SøkerIdDTO? {
        val søker: Søker = søkerRepository.findByIdent(ident) ?: return null
        søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
        return SøkerIdDTO(
            id = søker.søkerId.toString(),
        )
    }

    override fun hentIdent(søkerId: SøkerId, saksbehandler: Saksbehandler): String? {
        val søker: Søker = søkerRepository.hent(søkerId) ?: return null
        søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
        return søker.ident
    }
}
