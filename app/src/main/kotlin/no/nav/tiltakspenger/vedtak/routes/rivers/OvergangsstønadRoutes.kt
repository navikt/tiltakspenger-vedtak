package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.Feil
import no.nav.tiltakspenger.innsending.domene.OvergangsstønadVedtak
import no.nav.tiltakspenger.innsending.domene.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.OvergangsstønadMottattHendelse
import no.nav.tiltakspenger.innsending.service.ports.InnsendingMediator
import no.nav.tiltakspenger.libs.overgangsstonad.Feilmelding
import no.nav.tiltakspenger.libs.overgangsstonad.OvergangsstønadPeriodeDTO
import no.nav.tiltakspenger.libs.overgangsstonad.OvergangsstønadResponsDTO
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import java.time.LocalDate
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val overgangsstønadPath = "/rivers/overgangsstonad"

data class OvergangsstønadDTO(
    val ident: String,
    val journalpostId: String,
    val overgangsstønadRespons: OvergangsstønadResponsDTO,
    val innhentet: LocalDateTime,
)

fun Route.overgangsstønadRoutes(innsendingMediator: InnsendingMediator) {
    post("$overgangsstønadPath") {
        LOG.info { "Vi har mottatt overgangsstønad fra river" }
        val overgangsstønadDTO = call.receive<OvergangsstønadDTO>()

        when {
            overgangsstønadDTO.overgangsstønadRespons.feil != null -> {
                val feilMottattHendelse = FeilMottattHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = overgangsstønadDTO.journalpostId,
                    ident = overgangsstønadDTO.ident,
                    feil = when (overgangsstønadDTO.overgangsstønadRespons.feil!!) {
                        Feilmelding.Feilet -> Feil.Feilet
                        Feilmelding.IkkeHentet -> Feil.IkkeHentet
                        Feilmelding.IkkeTilgang -> Feil.IkkeTilgang
                        Feilmelding.FunksjonellFeil -> Feil.FunksjonellFeil
                    },
                )

                innsendingMediator.håndter(feilMottattHendelse)
                call.respond(message = "OK", status = HttpStatusCode.OK)
            }

            overgangsstønadDTO.overgangsstønadRespons.overgangsstønader != null -> {
                val overgangsstønadHendelse = OvergangsstønadMottattHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = overgangsstønadDTO.journalpostId,
                    ident = overgangsstønadDTO.ident,
                    innhentet = overgangsstønadDTO.innhentet,
                    overgangsstønadVedtakListe = overgangsstønadDTO.overgangsstønadRespons.overgangsstønader!!.map {
                        mapToOvergangsstønadVedtak(
                            dto = it,
                            innhentet = overgangsstønadDTO.innhentet,
                        )
                    },
                )

                SECURELOG.info { " Mottatt overgangsstønad og laget hendelse : $overgangsstønadHendelse" }
                innsendingMediator.håndter(overgangsstønadHendelse)
                call.respond(message = "OK", status = HttpStatusCode.OK)
            }

            else -> {
                throw IllegalStateException("Mottatt overgangsstønad som ikke her hverken vedtak eller feil")
            }
        }
    }
}

private fun mapToOvergangsstønadVedtak(
    dto: OvergangsstønadPeriodeDTO,
    innhentet: LocalDateTime,
): OvergangsstønadVedtak {
    return OvergangsstønadVedtak(
        id = OvergangsstønadVedtakId.random(),
        fom = LocalDate.parse(dto.fom), // TODO flytt parsing til faktainnhenter
        tom = LocalDate.parse(dto.tom),
        datakilde = Kilde.valueOf(dto.datakilde),
        innhentet = innhentet,
    )
}
