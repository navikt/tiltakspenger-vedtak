package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo

class BehandlingServiceImpl(
    private val behandlingRepo: BehandlingRepo,
) : BehandlingService {

    override fun hentBehandling(behandlingId: BehandlingId): Søknadsbehandling? {
        return behandlingRepo.hent(behandlingId)!!
    }

    override fun hentAlleBehandlinger(): List<Søknadsbehandling> {
        return behandlingRepo.hentAlle()
    }

    override fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning) {
        val behandling = hentBehandling(behandlingId)?.leggTilSaksopplysning(saksopplysning)
            ?: throw IllegalStateException("Kunne ikke legge til saksopplysning da vi ikke fant behandling $behandlingId")
        behandlingRepo.lagre(behandling)
    }
}
