package no.nav.tiltakspenger.saksbehandling.service.søker

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker
import no.nav.tiltakspenger.saksbehandling.ports.SøkerRepository

private val LOG = KotlinLogging.logger {}
val SECURELOG = KotlinLogging.logger("tjenestekall")

class SøkerServiceImpl(
    private val søkerRepository: SøkerRepository,
) : SøkerService {

    override fun hentSøkerIdOrNull(ident: String, saksbehandler: Saksbehandler): SøkerIdDTO? {
        val søker: Søker = søkerRepository.findByIdent(ident) ?: return null
        søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
        return SøkerIdDTO(
            id = søker.søkerId.toString(),
        )
    }

    override fun hentIdentOrNull(søkerId: SøkerId, saksbehandler: Saksbehandler): String? {
        val søker: Søker = søkerRepository.hent(søkerId) ?: return null
        søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
        return søker.ident
    }

    override fun hentSøkerId(ident: String, saksbehandler: Saksbehandler): SøkerIdDTO {
        return hentSøkerIdOrNull(ident, saksbehandler)
            ?: throw IkkeFunnetException("SøkerId ikke funnet")
                .also { SECURELOG.warn { "SøkerId ikke funnet for ident $ident og saksbehandler $saksbehandler" } }
    }

    override fun hentIdent(søkerId: SøkerId, saksbehandler: Saksbehandler): String {
        return hentIdentOrNull(søkerId, saksbehandler)
            ?: throw IkkeFunnetException("Ident ikke funnet")
                .also { SECURELOG.warn { "Ident ikke funnet for søkerId $søkerId og saksbehandler $saksbehandler" } }
    }
}
