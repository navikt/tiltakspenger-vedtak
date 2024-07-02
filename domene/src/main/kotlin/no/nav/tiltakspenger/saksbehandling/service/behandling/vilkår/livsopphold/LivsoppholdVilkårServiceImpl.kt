package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.leggTilLivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

class LivsoppholdVilkårServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) : LivsoppholdVilkårService {

    override fun leggTilSaksopplysning(command: LeggTilLivsoppholdSaksopplysningCommand): Førstegangsbehandling {
        val behandling =
            behandlingService.hentBehandling(command.behandlingId, command.saksbehandler) as Førstegangsbehandling
        return behandling.leggTilLivsoppholdSaksopplysning(command).also {
            behandlingRepo.lagre(it)
        }
    }
}
