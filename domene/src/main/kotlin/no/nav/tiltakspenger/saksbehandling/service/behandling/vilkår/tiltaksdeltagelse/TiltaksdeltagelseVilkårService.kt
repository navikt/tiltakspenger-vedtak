package no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.tiltaksdeltagelse

import arrow.core.Either
import arrow.core.left
import mu.KotlinLogging
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.KanIkkeLeggeTilSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltaksdeltagelse.LeggTilTiltaksdeltagelseKommando
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltaksdeltagelse.leggTilTiltaksdeltagelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService

class TiltaksdeltagelseVilkårService(
    private val behandlingRepo: BehandlingRepo,
    private val behandlingService: BehandlingService,
) {
    val logger = KotlinLogging.logger { }

    suspend fun oppdater(
        command: LeggTilTiltaksdeltagelseKommando,
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
        return behandling.leggTilTiltaksdeltagelseSaksopplysning(command).onRight {
            behandlingRepo.lagre(it)
        }
    }
}
