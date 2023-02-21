package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import no.nav.tiltakspenger.libs.overgangsstonad.OvergangsstønadResponsDTO
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
import no.nav.tiltakspenger.vedtak.meldinger.OvergangsstønadMottattHendelse
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
                // handle feil

                innsendingMediator.håndter(feilMottattHendelse)
                call.respond(message = "OK", status = HttpStatusCode.OK)
            }

            overgangsstønadDTO.overgangsstønadRespons.overgangsstønad != null -> {
                // handle ok
                val

                SECURELOG.info { " Mottatt overgangsstønad og laget hendelse : $overgangsstønadHendelse" }
                innsendingMediator.håndter(overgangsstønadHendelse)
                call.respond(message = "OK", status = HttpStatusCode.OK)
            }

            else -> {
                throw IllegalStateException("Mottatt overgangsstønad som ikke her hverken vedtak eller feil")
            }
        }


        if (overgangsstønadDTO.perioder.isNotEmpty()) {
            val overgangsstønadHendelse = OvergangsstønadMottattHendelse(
                aktivitetslogg = Aktivitetslogg(),
                journalpostId = overgangsstønadDTO.journalpostId,
                ident = overgangsstønadDTO.ident,
                innhentet = overgangsstønadDTO.innhentet,
                perioder = overgangsstønadDTO.perioder.map {
                    mapToOvergangsstønadVedtak(
                        periode = it,
                        innhentet = overgangsstønadDTO.innhentet,
                    )
                },
            )

        }

    }
}

private fun mapToOvergangsstønadVedtak(
    periode: OvergangsstønadPeriode,
    innhentet: LocalDateTime,
): OvergangsstønadVedtak {
    return OvergangsstønadVedtak(
        id = OvergangsstønadVedtakId.random(),
        periode = periode,
        innhentet = innhentet,
    )
}
