package no.nav.tiltakspenger.saksbehandling.service.søker

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker
import no.nav.tiltakspenger.saksbehandling.ports.SøkerRepository

private val LOG = KotlinLogging.logger {}
val SECURELOG = KotlinLogging.logger("tjenestekall")

class SøkerServiceImpl(
    private val søkerRepository: SøkerRepository,
) : SøkerService {

    override fun hentSøkerIdOrNull(fnr: Fnr, saksbehandler: Saksbehandler): SøkerIdDTO? {
        val søker: Søker = søkerRepository.findByIdent(fnr) ?: return null
        søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
        return SøkerIdDTO(
            id = søker.søkerId.toString(),
        )
    }

    override fun hentIdentOrNull(søkerId: SøkerId, saksbehandler: Saksbehandler): Fnr? {
        val søker: Søker = søkerRepository.hent(søkerId) ?: return null
        søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)
        return søker.fnr
    }

    override fun hentSøkerId(fnr: Fnr, saksbehandler: Saksbehandler): SøkerIdDTO {
        return hentSøkerIdOrNull(fnr, saksbehandler)
            ?: throw IkkeFunnetException("SøkerId ikke funnet")
                .also { SECURELOG.warn { "SøkerId ikke funnet for ident ${fnr.verdi} og saksbehandler $saksbehandler" } }
    }

    override fun hentIdent(søkerId: SøkerId, saksbehandler: Saksbehandler): Fnr {
        return hentIdentOrNull(søkerId, saksbehandler)
            ?: throw IkkeFunnetException("Ident ikke funnet")
                .also { SECURELOG.warn { "Ident ikke funnet for søkerId $søkerId og saksbehandler $saksbehandler" } }
    }
}
