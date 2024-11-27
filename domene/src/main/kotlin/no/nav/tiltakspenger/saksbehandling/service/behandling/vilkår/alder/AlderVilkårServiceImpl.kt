package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.alder

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import mu.KotlinLogging
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.KanIkkeLeggeTilSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.LeggTilAlderSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.leggTilAlderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

/** Brukes ikke i MVPen. */
class AlderVilkårServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) : AlderVilkårService {
    val logger = KotlinLogging.logger { }

    override suspend fun leggTilSaksopplysning(command: LeggTilAlderSaksopplysningCommand): Either<KanIkkeLeggeTilSaksopplysning, Behandling> {
        if (!command.saksbehandler.erSaksbehandler()) {
            logger.warn { "Navident ${command.saksbehandler.navIdent} med rollene ${command.saksbehandler.roller} har ikke tilgang til å legge til saksopplysninger" }
            return KanIkkeLeggeTilSaksopplysning.MåVæreSaksbehandler.left()
        }
        val behandling =
            behandlingService.hentBehandling(command.behandlingId, command.saksbehandler, correlationId = command.correlationId)
        return behandling.leggTilAlderSaksopplysning(command).also {
            behandlingRepo.lagre(it)
        }.right()
    }
}
