package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.OvergangsstønadDTO
import no.nav.tiltakspenger.vedtak.OvergangsstønadPeriode
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
import no.nav.tiltakspenger.vedtak.meldinger.OvergangsstønadMottattHendelse
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val overgangsstønadPath = "/rivers/overgangsstonad"

fun Route.overgangsstønadRoutes(innsendingMediator: InnsendingMediator) {
    post("$overgangsstønadPath") {
        LOG.info { "Vi har mottatt overgangsstønad fra river" }
        val overgangsstønadDTO = call.receive<OvergangsstønadDTO>()
        // todo: hva skal vi gjøre dersom vi ikke får noen perioder (men behovet er løst)?
        if (overgangsstønadDTO.perioder.isNotEmpty()) {
            val overgangsstønadHendelse = OvergangsstønadMottattHendelse(
                aktivitetslogg = Aktivitetslogg(),
                journalpostId = overgangsstønadDTO.journalpostId,
                ident = overgangsstønadDTO.ident,
                innhentet = overgangsstønadDTO.innhentet,
                perioder = overgangsstønadDTO.perioder.map {
                    mapToOvergangsstønadVedtak(
                        periode = it,
                        innhentet = overgangsstønadDTO.innhentet
                    )
                }
            )
            SECURELOG.info { " Mottatt overgangsstønad og laget hendelse : $overgangsstønadHendelse" }
            innsendingMediator.håndter(overgangsstønadHendelse)
        }
        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}

private fun mapToOvergangsstønadVedtak(periode: OvergangsstønadPeriode, innhentet: LocalDateTime): OvergangsstønadVedtak {
    return OvergangsstønadVedtak(
        id = OvergangsstønadVedtakId.random(),
        periode = periode,
        innhentet = innhentet
    )
}
