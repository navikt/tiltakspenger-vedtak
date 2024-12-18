package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold

import arrow.core.Either
import arrow.core.left
import mu.KotlinLogging
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.KanIkkeLeggeTilSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.leggTilLivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

class LivsoppholdVilkårServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) : LivsoppholdVilkårService {
    val logger = KotlinLogging.logger { }
    override suspend fun leggTilSaksopplysning(
        command: LeggTilLivsoppholdSaksopplysningCommand,
    ): Either<KanIkkeLeggeTilSaksopplysning, Behandling> {
        if (!command.saksbehandler.erSaksbehandler()) {
            logger.warn { "Navident ${command.saksbehandler.navIdent} med rollene ${command.saksbehandler.roller} har ikke tilgang til å legge til saksopplysninger" }
            return KanIkkeLeggeTilSaksopplysning.MåVæreSaksbehandler.left()
        }
        val behandling = behandlingService.hentBehandling(
            behandlingId = command.behandlingId,
            saksbehandler = command.saksbehandler,
            correlationId = command.correlationId,
        )
        return behandling.leggTilLivsoppholdSaksopplysning(command).onRight {
            behandlingRepo.lagre(it)
        }
    }
}
