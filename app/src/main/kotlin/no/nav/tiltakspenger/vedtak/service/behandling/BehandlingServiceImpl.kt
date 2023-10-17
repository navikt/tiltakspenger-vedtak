package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
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

    override fun sendTilBeslutter(behandlingId: BehandlingId, saksbehandler: String) {
        val behandling = hentBehandling(behandlingId)
        when (behandling) {
            is BehandlingVilkårsvurdert.Avslag -> behandlingRepo.lagre(behandling.tilBeslutting(saksbehandler))
            is BehandlingVilkårsvurdert.Innvilget -> behandlingRepo.lagre(behandling.tilBeslutting(saksbehandler))
            else -> throw IllegalStateException("Kunne ikke sende til beslutter da vi ikke fant behandling $behandlingId")
        }
    }
}
