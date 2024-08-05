package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.alder

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.LeggTilAlderSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.leggTilAlderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

/** Brukes ikke i MVPen. */
class AlderVilkårServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) : AlderVilkårService {

    override fun leggTilSaksopplysning(command: LeggTilAlderSaksopplysningCommand): Førstegangsbehandling {
        val behandling =
            behandlingService.hentBehandling(command.behandlingId, command.saksbehandler) as Førstegangsbehandling
        return behandling.leggTilAlderSaksopplysning(command).also {
            behandlingRepo.lagre(it)
        }
    }
}
