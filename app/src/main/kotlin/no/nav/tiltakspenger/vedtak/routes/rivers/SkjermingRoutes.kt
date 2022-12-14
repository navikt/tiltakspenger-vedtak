package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.rivers.YtelseSakDTO
import no.nav.tiltakspenger.vedtak.rivers.YtelseSakStatusEnum
import no.nav.tiltakspenger.vedtak.rivers.YtelseSakYtelsetypeEnum
import no.nav.tiltakspenger.vedtak.rivers.YtelseVedtakPeriodeTypeForYtelseEnum
import no.nav.tiltakspenger.vedtak.rivers.YtelseVedtakStatusEnum
import no.nav.tiltakspenger.vedtak.rivers.YtelseVedtakVedtakstypeEnum
import java.time.LocalDateTime

data class SkjermingDTO(
    val ident: String,
    val journalpostId: String,
    val skjerming: Boolean,
    val innhentet: LocalDateTime,
)

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val skjermingpath = "/rivers/skjerming"

fun Route.skjermingRoutes(innsendingMediator: InnsendingMediator) {
    post("$skjermingpath") {
        LOG.info { "Vi har mottatt skjerming fra river" }
        val skjermingDTO = call.receive<SkjermingDTO>()

        val skjermingHendelse = SkjermingMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = skjermingDTO.ident,
            journalpostId = skjermingDTO.journalpostId,
            skjerming = Skjerming(
                ident = skjermingDTO.ident,
                skjerming = skjermingDTO.skjerming,
                innhentet = skjermingDTO.innhentet,
            ),

        )
        SECURELOG.info {" Mottatt skjerming og laget hendelse : $skjermingHendelse" }
        innsendingMediator.håndter(skjermingHendelse)
        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
