package no.nav.tiltakspenger.vedtak.routes.datadeling

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.vedtak.RammevedtakService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.routes.withBody
import no.nav.tiltakspenger.vedtak.routes.withSystembruker
import java.time.LocalDate

internal const val DATADELING_PATH = "/datadeling"

fun Route.datadelingRoutes(
    behandlingService: BehandlingService,
    rammevedtakService: RammevedtakService,
    tokenService: TokenService,
) {
    val logger = KotlinLogging.logger {}

    post("$DATADELING_PATH/behandlinger") {
        logger.debug("Mottatt post-request på '$DATADELING_PATH/behandlinger' - systembruker henter behandlinger under behandling for gitt ident og periode")
        call.withSystembruker(tokenService = tokenService) { systembruker ->
            call.withBody<DatadelingDTO> { body ->
                body.toRequest().fold(
                    { call.respond(HttpStatusCode.BadRequest, it) },
                    {
                        val behandlinger = behandlingService.hentBehandlingerUnderBehandlingForIdent(
                            fnr = it.ident,
                            periode = Periode(it.fom, it.tom),
                            systembruker = systembruker,
                        )
                        call.respond(status = HttpStatusCode.OK, mapBehandlinger(behandlinger))
                    },
                )
            }
        }
    }

    post("$DATADELING_PATH/vedtak/perioder") {
        logger.debug("Mottatt post-request på '$DATADELING_PATH/vedtak/perioder' - systembruker henter perioder for vedtak for gitt ident og periode")
        call.withSystembruker(tokenService = tokenService) { systembruker ->
            call.withBody<DatadelingDTO> { body ->
                body.toRequest()
                    .fold(
                        { call.respond(HttpStatusCode.BadRequest, it) },
                        {
                            sikkerlogg.debug { "Henter perioder for vedtak med ident ${it.ident} for systembruker $systembruker, med periode fra ${it.fom} til ${it.tom}" }
                            val vedtak = rammevedtakService.hentVedtakForFnr(
                                fnr = it.ident,
                                periode = Periode(it.fom, it.tom),
                                systembruker = systembruker,
                            )
                            call.respond(status = HttpStatusCode.OK, mapVedtakPerioder(vedtak))
                        },
                    )
            }
        }
    }

    post("$DATADELING_PATH/vedtak/detaljer") {
        logger.debug("Mottatt post-request på '$DATADELING_PATH/vedtak/detaljer' - systembruker henter detaljer for vedtak for gitt ident og periode")
        call.withSystembruker(tokenService = tokenService) { systembruker ->
            call.withBody<DatadelingDTO> { body ->
                body.toRequest()
                    .fold(
                        { call.respond(HttpStatusCode.BadRequest, it) },
                        {
                            val vedtak = rammevedtakService.hentVedtakForFnr(
                                fnr = it.ident,
                                periode = Periode(it.fom, it.tom),
                                systembruker = systembruker,
                            )
                            call.respond(status = HttpStatusCode.OK, mapVedtak(vedtak))
                        },
                    )
            }
        }
    }
}

private fun mapBehandlinger(behandlinger: List<Behandling>): List<DatadelingBehandlingJsonResponse> =
    behandlinger.map {
        DatadelingBehandlingJsonResponse(
            behandlingId = it.id.toString(),
            fom = it.vurderingsperiode.fraOgMed,
            tom = it.vurderingsperiode.tilOgMed,
        )
    }

private fun mapVedtak(vedtak: List<Rammevedtak>): List<DatadelingVedtakJsonResponse> =
    vedtak.map {
        DatadelingVedtakJsonResponse(
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

private data class DatadelingVedtakJsonResponse(
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

private enum class Rettighet {
    TILTAKSPENGER,
    BARNETILLEGG,
    TILTAKSPENGER_OG_BARNETILLEGG,
    INGENTING,
}

private data class DatadelingVedtakPeriodeDTO(
    val vedtakId: String,
    val fom: LocalDate,
    val tom: LocalDate,
)

private data class DatadelingBehandlingJsonResponse(
    val behandlingId: String,
    val fom: LocalDate,
    val tom: LocalDate,
)
