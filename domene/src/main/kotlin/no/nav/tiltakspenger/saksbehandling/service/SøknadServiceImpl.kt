package no.nav.tiltakspenger.saksbehandling.service

import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo

class SøknadServiceImpl(
    private val søknadRepo: SøknadRepo,
) : SøknadService {
    override fun nySøknad(søknad: Søknad) {
        søknadRepo.lagre(søknad)
    }

    override fun hentSøknad(søknadId: SøknadId): Søknad {
        return søknadRepo.hentForSøknadId(søknadId)
    }
}
