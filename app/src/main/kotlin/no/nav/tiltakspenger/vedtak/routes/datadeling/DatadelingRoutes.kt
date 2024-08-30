package no.nav.tiltakspenger.vedtak.routes.datadeling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.vedtak.RammevedtakService
import java.time.LocalDate

private val SECURELOG = KotlinLogging.logger("tjenestekall")
private val LOG = KotlinLogging.logger {}

internal const val DATADELING_PATH = "/datadeling"

fun Route.datadelingRoutes(
    behandlingService: BehandlingService,
    rammevedtakService: RammevedtakService,
) {
    post("$DATADELING_PATH/behandlinger") {
        SECURELOG.debug("Mottatt request på $DATADELING_PATH/behadlinger")

        call.receive<DatadelingDTO>().toRequest()
            .fold(
                { call.respond(HttpStatusCode.BadRequest, it) },
                {
                    val behandlinger = behandlingService.hentBehandlingerUnderBehandlingForIdent(
                        ident = it.ident,
                        fom = it.fom,
                        tom = it.tom,
                    )
                    call.respond(status = HttpStatusCode.OK, mapBehandlinger(behandlinger))
                },
            )
    }

    post("$DATADELING_PATH/vedtak") {
        SECURELOG.debug("Mottatt request på $DATADELING_PATH/vedtak")

        call.receive<DatadelingDTO>().toRequest()
            .fold(
                { call.respond(HttpStatusCode.BadRequest, it) },
                {
                    val vedtak = rammevedtakService.hentVedtakForIdent(
                        ident = it.ident,
                        fom = it.fom,
                        tom = it.tom,
                    )
                    call.respond(status = HttpStatusCode.OK, mapVedtak(vedtak))
                },
            )
    }
}

private fun mapBehandlinger(behandlinger: List<Behandling>): List<DatadelingBehandlingDTO> {
    return behandlinger.map {
        DatadelingBehandlingDTO(
            behandlingId = it.id.toString(),
            fom = it.vurderingsperiode.fraOgMed,
            tom = it.vurderingsperiode.tilOgMed,
        )
    }
}

private fun mapVedtak(vedtak: List<Rammevedtak>): List<DatadelingVedtakDTO> {
    return vedtak.map {
        DatadelingVedtakDTO(
            vedtakId = it.id.toString(),
            fom = it.periode.fraOgMed,
            tom = it.periode.tilOgMed,
        )
    }
}

data class DatadelingVedtakDTO(
    val vedtakId: String,
    val fom: LocalDate,
    val tom: LocalDate,
)

data class DatadelingBehandlingDTO(
    val behandlingId: String,
    val fom: LocalDate,
    val tom: LocalDate,
)
