package no.nav.tiltakspenger.vedtak.routes.behandling.benk

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
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
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling.HarAlleredeStartetBehandlingen
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling.HarIkkeTilgangTilPerson
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling.OppretteBehandling
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingerPath
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.ExceptionResponse
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.behandlingBenkRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
) {
    get(behandlingerPath) {
        SECURELOG.debug("Mottatt request for å hente alle behandlinger på benken")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        val behandlinger = behandlingService.hentSaksoversikt(saksbehandler).fraBehandlingToBehandlingBenkDto()

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }

    post("$behandlingPath/startbehandling") {
        SECURELOG.debug { "Mottatt request for å starte behandlingen. Knytter også saksbehandleren til behandlingen." }
        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val søknadId = SøknadId.fromString(call.receive<BehandlingIdDTO>().id)

        sakService.startFørstegangsbehandling(søknadId, saksbehandler).fold(
            {
                when (it) {
                    is HarIkkeTilgangTilPerson -> {
                        call.respond(HttpStatusCode.Forbidden, "Saksbehandler har ikke tilgang til person")
                    }

                    is HarAlleredeStartetBehandlingen -> {
                        call.respond(HttpStatusCode.OK, BehandlingIdDTO(it.behandlingId.toString()))
                    }

                    is OppretteBehandling -> when (it.underliggende) {
                        FantIkkeTiltak -> call.respond(
                            HttpStatusCode.InternalServerError,
                            ExceptionResponse(
                                500,
                                "fant-ikke-tiltak",
                                "Fant ikke igjen tiltaket det er søkt på i tiltak knyttet til brukeren",
                            ),
                        )

                        StøtterIkkeBarnetillegg -> call.respond(
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
                call.respond(HttpStatusCode.OK, BehandlingIdDTO(it.førstegangsbehandling.id.toString()))
            },
        )
    }

    post("$behandlingPath/tabehandling") {
        SECURELOG.debug { "Mottatt request om å sette saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.receive<BehandlingIdDTO>().id)

        behandlingService.taBehandling(behandlingId, saksbehandler)

        val response = BehandlingIdDTO(behandlingId.toString())
        call.respond(status = HttpStatusCode.OK, response)
    }

    // TODO jah: Kommenterer ut denne inntil videre. Søk på fnr vil ikke lengre fungere for frontend
//    get("$behandlingerPath/hentForIdent/{søkerId}") {
//        SECURELOG.debug { "Mottatt request om å hente alle behandlinger for en ident" }
//
//        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
//        val søkerId = SøkerId.fromString(call.parameter("søkerId"))
//        val ident = søkerService.hentIdent(søkerId, saksbehandler)
//
//        val behandlinger = behandlingService.hentBehandlingForIdent(ident, saksbehandler)
//            .mapBehandlinger()
//
//        call.respond(status = HttpStatusCode.OK, behandlinger)
//    }
}
