package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.introduksjonsprogrammet

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import mu.KotlinLogging
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.KanIkkeLeggeTilSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.LeggTilIntroSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.leggTilIntroSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

/** Brukes ikke i MVPen. */
class IntroVilkårServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) : IntroVilkårService {
    val logger = KotlinLogging.logger { }
    override suspend fun leggTilSaksopplysning(command: LeggTilIntroSaksopplysningCommand): Either<KanIkkeLeggeTilSaksopplysning, Behandling> {
        if (!command.saksbehandler.erSaksbehandler()) {
            logger.warn { "Navident ${command.saksbehandler.navIdent} med rollene ${command.saksbehandler.roller} har ikke tilgang til å legge til saksopplysninger" }
            return KanIkkeLeggeTilSaksopplysning.MåVæreSaksbehandler.left()
        }
        val behandling =
            behandlingService.hentBehandling(command.behandlingId, command.saksbehandler, correlationId = command.correlationId)
        return behandling.leggTilIntroSaksopplysning(command).also {
            behandlingRepo.lagre(it)
        }.right()
    }
}
