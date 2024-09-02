package no.nav.tiltakspenger.vedtak.routes.meldekort

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.meldekort.service.HentMeldekortService
import no.nav.tiltakspenger.vedtak.routes.meldekort.dto.toDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.hentMeldekortRoute(
    hentMeldekortService: HentMeldekortService,
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
) {
    val logger = KotlinLogging.logger { }

    get("/sak/{sakId}/meldekort") {
        logger.info("Mottatt request på /sak/{sakId}/meldekort - henter alle meldekort for sak")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val sakId =
            call.parameters["sakId"]
                ?: return@get call.respond(message = "sakId mangler", status = HttpStatusCode.NotFound)
        val meldekortperioder = hentMeldekortService.hentForSakId(SakId.fromString(sakId), saksbehandler)

        val message = meldekortperioder.toDTO()
        logger.info { "respons på request /sak/{sakId}/meldekort - henter alle meldekort for sak: $message" }
        call.respond(status = HttpStatusCode.OK, message = message)
    }

    get("/sak/{sakId}/meldekort/{meldekortId}") {
        logger.info("Motatt request på /sak/{sakId}/meldekort/{meldekortId}")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val meldekortId =
            call.parameters["meldekortId"]
                ?: return@get call.respond(message = "meldekortId mangler", status = HttpStatusCode.NotFound)
        val meldekort = hentMeldekortService.hentForMeldekortId(MeldekortId.fromString(meldekortId), saksbehandler)
        checkNotNull(meldekort) { "Meldekort med id $meldekortId eksisterer ikke i databasen" }
        call.respond(status = HttpStatusCode.OK, message = meldekort.toDTO())
    }
}
