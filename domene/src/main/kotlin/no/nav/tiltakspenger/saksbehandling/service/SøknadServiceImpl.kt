package no.nav.tiltakspenger.saksbehandling.service

import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo

class SøknadServiceImpl(
    private val søknadRepo: SøknadRepo,
) : SøknadService {
    override fun nySøknad(søknad: Søknad, systembruker: Systembruker) {
        require(systembruker.roller.harRolle(Rolle.LAGE_HENDELSER)) { "Systembruker mangler rollen LAGE_HENDELSER. Systembrukers roller: ${systembruker.roller}" }
        søknadRepo.lagre(søknad)
    }

    override fun hentSøknad(søknadId: SøknadId): Søknad {
        return søknadRepo.hentForSøknadId(søknadId)
    }
}
