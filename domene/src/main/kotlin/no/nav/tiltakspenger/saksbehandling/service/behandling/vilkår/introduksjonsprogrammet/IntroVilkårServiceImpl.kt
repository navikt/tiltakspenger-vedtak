package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.introduksjonsprogrammet

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.LeggTilIntroSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.leggTilIntroSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

class IntroVilkårServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) : IntroVilkårService {

    override fun leggTilSaksopplysning(command: LeggTilIntroSaksopplysningCommand): Førstegangsbehandling {
        val behandling =
            behandlingService.hentBehandling(command.behandlingId, command.saksbehandler) as Førstegangsbehandling
        return behandling.leggTilIntroSaksopplysning(command).also {
            behandlingRepo.lagre(it)
        }
    }
}
