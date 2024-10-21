package no.nav.tiltakspenger.vedtak.routes.behandling.benk

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling.FantIkkeTiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling.StøtterIkkeBarnetillegg
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling.StøtterKunInnvilgelse
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.sak.KanIkkeStarteFørstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLINGER_PATH
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.withBody
import no.nav.tiltakspenger.vedtak.routes.withSaksbehandler

fun Route.behandlingBenkRoutes(
    tokenService: TokenService,
    behandlingService: BehandlingService,
    sakService: SakService,
    auditService: AuditService,
) {
    val logger = KotlinLogging.logger {}

    get(BEHANDLINGER_PATH) {
        logger.debug("Mottatt get-request på $BEHANDLINGER_PATH for å hente alle behandlinger på benken")
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            val behandlinger = sakService.hentSaksoversikt(saksbehandler).fraBehandlingToBehandlingBenkDto()
            call.respond(status = HttpStatusCode.OK, behandlinger)
        }
    }

    post("$BEHANDLING_PATH/startbehandling") {
        logger.debug { "Mottatt post-request på '$BEHANDLING_PATH/startbehandling' - Starter behandlingen og knytter til sak. Knytter også saksbehandleren til behandlingen." }
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            // TODO post-mvp jah: Kan ikke søknadId ligge i pathen?
            call.withBody<BehandlingIdDTO> { body ->
                val søknadId = SøknadId.fromString(body.id)
                val correlationId = call.correlationId()
                sakService.startFørstegangsbehandling(søknadId, saksbehandler, correlationId = correlationId).fold(
                    {
                        when (it) {
                            is KanIkkeStarteFørstegangsbehandling.HarAlleredeStartetBehandlingen -> {
                                call.respond(HttpStatusCode.OK, BehandlingIdDTO(it.behandlingId.toString()))
                            }

                            is KanIkkeStarteFørstegangsbehandling.OppretteBehandling ->
                                when (it.underliggende) {
                                    FantIkkeTiltak ->
                                        call.respond(
                                            message = "Fant ikke igjen tiltaket det er søkt på i tiltak knyttet til brukeren",
                                            status = HttpStatusCode.InternalServerError,
                                        )

                                    StøtterIkkeBarnetillegg ->
                                        call.respond(
                                            message = "Vi støtter ikke at brukeren har barn i PDL eller manuelle barn.",
                                            status = HttpStatusCode.BadRequest,
                                        )

                                    is StøtterKunInnvilgelse -> call.respond(
                                        message = "Vi støtter ikke å opprette en behandling som vil føre til delvis innvilgelse eller avslag.",
                                        status = HttpStatusCode.BadRequest,
                                    )
                                }
                        }
                    },
                    {
                        auditService.logForSøknadId(
                            søknadId = søknadId,
                            navIdent = saksbehandler.navIdent,
                            action = AuditLogEvent.Action.CREATE,
                            contextMessage = "Oppretter behandling fra søknad og starter behandlingen",
                            correlationId = correlationId,
                        )

                        call.respond(HttpStatusCode.OK, BehandlingIdDTO(it.førstegangsbehandling.id.toString()))
                    },
                )
            }
        }
    }

    post("$BEHANDLING_PATH/tabehandling") {
        logger.debug { "Mottatt post-request på '$BEHANDLING_PATH/tabehandling' - Knytter saksbehandler/beslutter til behandlingen." }
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            // TODO post-mvp jah: Kan ikke behandlingId ligge i pathen?
            val behandlingId = BehandlingId.fromString(call.receive<BehandlingIdDTO>().id)

            val correlationId = call.correlationId()
            val behandling = behandlingService.taBehandling(behandlingId, saksbehandler, correlationId = correlationId).toDTO()

            auditService.logMedBehandlingId(
                behandlingId = behandlingId,
                navIdent = saksbehandler.navIdent,
                action = AuditLogEvent.Action.UPDATE,
                contextMessage = "Saksbehandler tar behandlingen",
                correlationId = correlationId,
            )

            call.respond(status = HttpStatusCode.OK, behandling)
        }
    }
}
