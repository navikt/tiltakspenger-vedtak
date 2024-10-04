package no.nav.tiltakspenger.vedtak.routes.behandling.benk

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling.FantIkkeTiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling.StøtterIkkeBarnetillegg
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.sak.KanIkkeStarteFørstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLINGER_PATH
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.ExceptionResponse
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.behandlingBenkRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
    auditService: AuditService,
) {
    get(BEHANDLINGER_PATH) {
        sikkerlogg.debug("Mottatt request for å hente alle behandlinger på benken")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        val behandlinger = sakService.hentSaksoversikt(saksbehandler).fraBehandlingToBehandlingBenkDto()

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }

    post("$BEHANDLING_PATH/startbehandling") {
        sikkerlogg.debug { "Mottatt request for å starte behandlingen. Knytter også saksbehandleren til behandlingen." }
        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val søknadId = SøknadId.fromString(call.receive<BehandlingIdDTO>().id)

        sakService.startFørstegangsbehandling(søknadId, saksbehandler, correlationId = CorrelationId.generate()).fold(
            {
                when (it) {
                    is KanIkkeStarteFørstegangsbehandling.HarAlleredeStartetBehandlingen -> {
                        call.respond(HttpStatusCode.OK, BehandlingIdDTO(it.behandlingId.toString()))
                    }

                    is KanIkkeStarteFørstegangsbehandling.OppretteBehandling ->
                        when (it.underliggende) {
                            FantIkkeTiltak ->
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ExceptionResponse(
                                        500,
                                        "fant-ikke-tiltak",
                                        "Fant ikke igjen tiltaket det er søkt på i tiltak knyttet til brukeren",
                                    ),
                                )

                            StøtterIkkeBarnetillegg ->
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ExceptionResponse(
                                        400,
                                        "støtter-ikke-barnetillegg",
                                        "Vi støtter ikke at brukeren har barn i PDL eller manuelle barn.",
                                    ),
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
                    callId = call.callId,
                )

                call.respond(HttpStatusCode.OK, BehandlingIdDTO(it.førstegangsbehandling.id.toString()))
            },
        )
    }

    post("$BEHANDLING_PATH/tabehandling") {
        sikkerlogg.debug { "Mottatt request om å sette saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.receive<BehandlingIdDTO>().id)

        behandlingService.taBehandling(behandlingId, saksbehandler, correlationId = CorrelationId.generate())

        val response = BehandlingIdDTO(behandlingId.toString())

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.UPDATE,
            contextMessage = "Saksbehandler tar behandlingen",
            callId = call.callId,
        )

        call.respond(status = HttpStatusCode.OK, response)
    }
}
