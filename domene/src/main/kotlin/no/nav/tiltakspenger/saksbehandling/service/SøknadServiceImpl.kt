package no.nav.tiltakspenger.saksbehandling.service

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

class SøknadServiceImpl(
    private val søknadRepo: SøknadRepo,
    private val sakRepo: SakRepo,
    private val behandlingService: BehandlingService,
) : SøknadService {
    override fun nySøknad(søknad: Søknad) {
        val alleredeStartetBehandlingen = behandlingService.hentBehandlingForSøknadId(søknad.id) === null
        val alleredeSakPåPerson = sakRepo.hentForIdent(søknad.personopplysninger.fnr).isNotEmpty()
        val kanBehandles = søknad.kanBehandles() && !alleredeStartetBehandlingen && !alleredeSakPåPerson
        søknadRepo.lagre(søknad, kanBehandles)
    }
}
