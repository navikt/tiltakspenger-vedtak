package no.nav.tiltakspenger.vedtak.routes.datadeling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.vedtak.RammevedtakService
import java.time.LocalDate

private val LOG = KotlinLogging.logger {}

internal const val DATADELING_PATH = "/datadeling"

fun Route.datadelingRoutes(
    behandlingService: BehandlingService,
    rammevedtakService: RammevedtakService,
) {
    post("$DATADELING_PATH/behandlinger") {
        sikkerlogg.debug("Mottatt request på $DATADELING_PATH/behadlinger")

        call
            .receive<DatadelingDTO>()
            .toRequest()
            .fold(
                { call.respond(HttpStatusCode.BadRequest, it) },
                {
                    val behandlinger =
                        behandlingService.hentBehandlingerUnderBehandlingForIdent(
                            ident = it.ident,
                            fom = it.fom,
                            tom = it.tom,
                        )
                    call.respond(status = HttpStatusCode.OK, mapBehandlinger(behandlinger))
                },
            )
    }

    post("$DATADELING_PATH/vedtak/perioder") {
        sikkerlogg.debug("Mottatt request på $DATADELING_PATH/vedtak/perioder")

        call
            .receive<DatadelingDTO>()
            .toRequest()
            .fold(
                { call.respond(HttpStatusCode.BadRequest, it) },
                {
                    sikkerlogg.info { "Henter perioder for vedtak med ident ${it.ident}, med periode fra ${it.fom} til ${it.tom}" }
                    val vedtak =
                        rammevedtakService.hentVedtakForIdent(
                            ident = it.ident,
                            fom = it.fom,
                            tom = it.tom,
                        )
                    call.respond(status = HttpStatusCode.OK, mapVedtakPerioder(vedtak))
                },
            )
    }

    post("$DATADELING_PATH/vedtak/detaljer") {
        sikkerlogg.debug("Mottatt request på $DATADELING_PATH/vedtak/detaljer")

        call
            .receive<DatadelingDTO>()
            .toRequest()
            .fold(
                { call.respond(HttpStatusCode.BadRequest, it) },
                {
                    val vedtak =
                        rammevedtakService.hentVedtakForIdent(
                            ident = it.ident,
                            fom = it.fom,
                            tom = it.tom,
                        )
                    call.respond(status = HttpStatusCode.OK, mapVedtak(vedtak))
                },
            )
    }
}

private fun mapBehandlinger(behandlinger: List<Behandling>): List<DatadelingBehandlingDTO> =
    behandlinger.map {
        DatadelingBehandlingDTO(
            behandlingId = it.id.toString(),
            fom = it.vurderingsperiode.fraOgMed,
            tom = it.vurderingsperiode.tilOgMed,
        )
    }

private fun mapVedtak(vedtak: List<Rammevedtak>): List<DatadelingVedtakDTO> =
    vedtak.map {
        DatadelingVedtakDTO(
            vedtakId = it.id.toString(),
            fom = it.periode.fraOgMed,
            tom = it.periode.tilOgMed,
            sakId = it.sakId.toString(),
            saksnummer = it.saksnummer.toString(),
            // TODO pre-mvp: Resten av feltene.
            antallDager = 0.0,
            dagsatsTiltakspenger = 0,
            dagsatsBarnetillegg = 0,
            antallBarn = 0,
            relaterteTiltak = "",
            rettighet = Rettighet.TILTAKSPENGER,
        )
    }

private fun mapVedtakPerioder(vedtak: List<Rammevedtak>): List<DatadelingVedtakPeriodeDTO> =
    vedtak.map {
        DatadelingVedtakPeriodeDTO(
            vedtakId = it.id.toString(),
            fom = it.periode.fraOgMed,
            tom = it.periode.tilOgMed,
        )
    }

data class DatadelingVedtakDTO(
    val vedtakId: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val antallDager: Double,
    val dagsatsTiltakspenger: Int,
    val dagsatsBarnetillegg: Int,
    val antallBarn: Int,
    val relaterteTiltak: String,
    val rettighet: Rettighet,
    val sakId: String,
    val saksnummer: String,
)

enum class Rettighet {
    TILTAKSPENGER,
    BARNETILLEGG,
    TILTAKSPENGER_OG_BARNETILLEGG,
    INGENTING,
}

data class DatadelingVedtakPeriodeDTO(
    val vedtakId: String,
    val fom: LocalDate,
    val tom: LocalDate,
)

data class DatadelingBehandlingDTO(
    val behandlingId: String,
    val fom: LocalDate,
    val tom: LocalDate,
)
