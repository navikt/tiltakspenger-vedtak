package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.LeggTilKvpSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.leggTilKvpSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

class KvpVilkårServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) : KvpVilkårService {
    override suspend fun leggTilSaksopplysning(command: LeggTilKvpSaksopplysningCommand): Førstegangsbehandling {
        val behandling =
            behandlingService.hentBehandling(command.behandlingId, command.saksbehandler, command.correlationId) as Førstegangsbehandling
        return behandling.leggTilKvpSaksopplysning(command).also {
            behandlingRepo.lagre(it)
        }
    }
}
