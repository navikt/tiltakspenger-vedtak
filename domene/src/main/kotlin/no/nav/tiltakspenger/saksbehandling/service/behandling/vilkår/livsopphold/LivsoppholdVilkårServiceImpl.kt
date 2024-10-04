package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold

import arrow.core.Either
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.leggTilLivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

class LivsoppholdVilkårServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) : LivsoppholdVilkårService {
    override suspend fun leggTilSaksopplysning(
        command: LeggTilLivsoppholdSaksopplysningCommand,
    ): Either<LivsoppholdVilkår.PeriodenMåVæreLikVurderingsperioden, Førstegangsbehandling> {
        val behandling =
            behandlingService.hentBehandling(command.behandlingId, command.saksbehandler, correlationId = command.correlationId) as Førstegangsbehandling
        return behandling.leggTilLivsoppholdSaksopplysning(command).onRight {
            behandlingRepo.lagre(it)
        }
    }
}
