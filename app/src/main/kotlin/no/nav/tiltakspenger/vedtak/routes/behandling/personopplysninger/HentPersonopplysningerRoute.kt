package no.nav.tiltakspenger.vedtak.routes.behandling.personopplysninger

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

// TODO pre-mvp B: Midlertidig løsning for å ikke brekke dev. Denne skal skrives om til å hente personalia direkte fra pdl.
fun Route.hentPersonRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    sakService: SakService,
) {
    get("$BEHANDLING_PATH/{behandlingId}/personopplysninger") {
        SECURELOG.debug("Mottatt request på $BEHANDLING_PATH/{behandlingId}/personopplysninger")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        val sak = sakService.hentMedBehandlingId(behandlingId, saksbehandler)

        val personopplysninger = sak.personopplysninger.søker().toDTO()

        call.respond(status = HttpStatusCode.OK, personopplysninger)
    }
}
