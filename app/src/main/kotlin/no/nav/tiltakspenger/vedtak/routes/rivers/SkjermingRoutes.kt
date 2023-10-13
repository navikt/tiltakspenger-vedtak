package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.skjerming.Feilmelding.IdentIkkeFunnet
import no.nav.tiltakspenger.libs.skjerming.SkjermingResponsDTO
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Feil
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.SkjermingPerson
import no.nav.tiltakspenger.vedtak.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.service.sak.SakService
import java.time.LocalDateTime

data class SkjermingDTO(
    val ident: String,
    val journalpostId: String,
    val skjerming: SkjermingResponsDTO,
    val innhentet: LocalDateTime,
)

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val skjermingpath = "/rivers/skjerming"

fun Route.skjermingRoutes(
    innsendingMediator: InnsendingMediator,
    sakService: SakService,
) {
    post("$skjermingpath") {
        LOG.info { "Vi har mottatt skjerming fra river" }
        val skjermingDTO = call.receive<SkjermingDTO>()

        when {
            skjermingDTO.skjerming.feil != null -> {
                val feilMottattHendelse = FeilMottattHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = skjermingDTO.journalpostId,
                    ident = skjermingDTO.ident,
                    feil = when (skjermingDTO.skjerming.feil!!) {
                        IdentIkkeFunnet -> Feil.IdentIkkeFunnet
                    },
                )
                innsendingMediator.håndter(feilMottattHendelse)
                call.respond(message = "OK", status = HttpStatusCode.OK)
            }

            skjermingDTO.skjerming.skjermingForPersoner != null -> {
                skjermingDTO.skjerming.skjermingForPersoner?.let { dto ->
                    val skjerming = mapSkjerming(dto, skjermingDTO.innhentet)

                    sakService.mottaSkjerming(skjermingDTO.journalpostId, skjerming)

                    val skjermingHendelse = SkjermingMottattHendelse(
                        aktivitetslogg = Aktivitetslogg(),
                        journalpostId = skjermingDTO.journalpostId,
                        ident = skjermingDTO.ident,
                        skjerming = skjerming,
                        tidsstempelSkjermingInnhentet = skjermingDTO.innhentet,
                    )
                    SECURELOG.info { " Mottatt skjerming og laget hendelse : $skjermingHendelse" }
                    innsendingMediator.håndter(skjermingHendelse)
                    call.respond(message = "OK", status = HttpStatusCode.OK)
                }
            }

            else ->
                throw IllegalStateException("Mottatt en skjerming som ikke har hverken skjerming eller feil")
        }
    }
}

private fun mapSkjerming(
    dto: no.nav.tiltakspenger.libs.skjerming.SkjermingDTO,
    innhentet: LocalDateTime,
): Skjerming {
    return Skjerming(
        søker = SkjermingPerson(
            ident = dto.søker.ident,
            skjerming = dto.søker.skjerming,
        ),
        barn = dto.barn.map { barn ->
            SkjermingPerson(
                ident = barn.ident,
                skjerming = barn.skjerming,
            )
        },
        innhentet = innhentet,
    )
}
