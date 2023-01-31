package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.felles.ForeldrepengerVedtakId
import no.nav.tiltakspenger.libs.fp.FPResponsDTO
import no.nav.tiltakspenger.libs.fp.FPResponsDTO.YtelserOutput
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Feil
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak.YtelserOutput.PLEIEPENGER_SYKT_BARN
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.ForeldrepengerMottattHendelse
import java.time.LocalDateTime

data class ForeldrepengerDTO(
    val ident: String,
    val journalpostId: String,
    val foreldrepenger: FPResponsDTO,
    val innhentet: LocalDateTime,
)

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val foreldrepengerpath = "/rivers/foreldrepenger"

fun Route.foreldrepengerRoutes(innsendingMediator: InnsendingMediator) {
    post("$foreldrepengerpath") {
        LOG.info { "Vi har mottatt foreldrepenger fra river" }
        val foreldrepengerDTO = call.receive<ForeldrepengerDTO>()

        when {
            foreldrepengerDTO.foreldrepenger.feil != null -> {
                val feilMottattHendelse = FeilMottattHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = foreldrepengerDTO.journalpostId,
                    ident = foreldrepengerDTO.ident,
                    feil = when (foreldrepengerDTO.foreldrepenger.feil!!) {
                        FPResponsDTO.FeilmeldingDTO.UkjentFeil -> Feil.UkjentFeil
                    },
                )
                innsendingMediator.håndter(feilMottattHendelse)
                call.respond(message = "OK", status = HttpStatusCode.OK)
            }

            foreldrepengerDTO.foreldrepenger.ytelser != null -> {
                foreldrepengerDTO.foreldrepenger.ytelser?.let { dto ->
                    val foreldrepengerHendelse = ForeldrepengerMottattHendelse(
                        aktivitetslogg = Aktivitetslogg(),
                        journalpostId = foreldrepengerDTO.journalpostId,
                        ident = foreldrepengerDTO.ident,
                        foreldrepengerVedtakListe = dto.map {
                            mapFPYtelser(
                                ytelseV1DTO = it,
                                innhentet = foreldrepengerDTO.innhentet,
                            )
                        },
                        tidsstempelForeldrepengerVedtakInnhentet = foreldrepengerDTO.innhentet,
                    )
                    SECURELOG.info { " Mottatt foreldrepenger og laget hendelse : $foreldrepengerHendelse" }
                    innsendingMediator.håndter(foreldrepengerHendelse)
                    call.respond(message = "OK", status = HttpStatusCode.OK)
                }
            }

            else ->
                throw IllegalStateException("Mottatt en foreldrepenger som ikke har hverken ytelser eller feil")
        }
    }
}

private fun mapFPYtelser(ytelseV1DTO: FPResponsDTO.YtelseV1DTO, innhentet: LocalDateTime): ForeldrepengerVedtak {
    return ForeldrepengerVedtak(
        id = ForeldrepengerVedtakId.random(),
        version = ytelseV1DTO.version,
        aktør = ytelseV1DTO.aktør,
        vedtattTidspunkt = ytelseV1DTO.vedtattTidspunkt,
        ytelse = when (ytelseV1DTO.ytelse) {
            YtelserOutput.PLEIEPENGER_SYKT_BARN -> PLEIEPENGER_SYKT_BARN
            YtelserOutput.PLEIEPENGER_NÆRSTÅENDE -> ForeldrepengerVedtak.YtelserOutput.PLEIEPENGER_NÆRSTÅENDE
            YtelserOutput.OMSORGSPENGER -> ForeldrepengerVedtak.YtelserOutput.OMSORGSPENGER
            YtelserOutput.OPPLÆRINGSPENGER -> ForeldrepengerVedtak.YtelserOutput.OPPLÆRINGSPENGER
            YtelserOutput.ENGANGSTØNAD -> ForeldrepengerVedtak.YtelserOutput.ENGANGSTØNAD
            YtelserOutput.FORELDREPENGER -> ForeldrepengerVedtak.YtelserOutput.FORELDREPENGER
            YtelserOutput.SVANGERSKAPSPENGER -> ForeldrepengerVedtak.YtelserOutput.SVANGERSKAPSPENGER
            YtelserOutput.FRISINN -> ForeldrepengerVedtak.YtelserOutput.FRISINN
        },
        saksnummer = ytelseV1DTO.saksnummer,
        vedtakReferanse = ytelseV1DTO.vedtakReferanse,
        ytelseStatus = when (ytelseV1DTO.ytelseStatus) {
            FPResponsDTO.Status.UNDER_BEHANDLING -> ForeldrepengerVedtak.Status.UNDER_BEHANDLING
            FPResponsDTO.Status.LØPENDE -> ForeldrepengerVedtak.Status.LØPENDE
            FPResponsDTO.Status.AVSLUTTET -> ForeldrepengerVedtak.Status.AVSLUTTET
            FPResponsDTO.Status.UKJENT -> ForeldrepengerVedtak.Status.UKJENT
        },
        kildesystem = when (ytelseV1DTO.kildesystem) {
            FPResponsDTO.Kildesystem.FPSAK -> ForeldrepengerVedtak.Kildesystem.FPSAK
            FPResponsDTO.Kildesystem.K9SAK -> ForeldrepengerVedtak.Kildesystem.K9SAK
        },
        periode = Periode(fra = ytelseV1DTO.periode.fom, til = ytelseV1DTO.periode.tom),
        tilleggsopplysninger = ytelseV1DTO.tilleggsopplysninger,
        anvist = ytelseV1DTO.anvist.map {
            ForeldrepengerVedtak.ForeldrepengerAnvisning(
                periode = Periode(fra = it.periode.fom, til = it.periode.tom),
                beløp = it.beløp,
                dagsats = it.dagsats,
                utbetalingsgrad = it.utbetalingsgrad,
            )
        },
        innhentet = innhentet,
    )
}