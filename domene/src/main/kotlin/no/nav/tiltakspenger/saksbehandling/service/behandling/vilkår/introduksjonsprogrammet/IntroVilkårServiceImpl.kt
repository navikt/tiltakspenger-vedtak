package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.introduksjonsprogrammet

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.LeggTilIntroSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.leggTilIntroSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

/** Brukes ikke i MVPen. */
class IntroVilkårServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) : IntroVilkårService {
    override suspend fun leggTilSaksopplysning(command: LeggTilIntroSaksopplysningCommand): Førstegangsbehandling {
        val behandling =
            behandlingService.hentBehandling(command.behandlingId, command.saksbehandler, correlationId = command.correlationId) as Førstegangsbehandling
        return behandling.leggTilIntroSaksopplysning(command).also {
            behandlingRepo.lagre(it)
        }
    }
}
