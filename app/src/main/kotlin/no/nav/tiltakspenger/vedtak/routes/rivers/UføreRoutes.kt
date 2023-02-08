package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.UføreVedtakId
import no.nav.tiltakspenger.libs.ufore.Feilmelding
import no.nav.tiltakspenger.libs.ufore.UforeResponsDTO
import no.nav.tiltakspenger.libs.ufore.UføregradDTO
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Feil
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.UføreVedtak
import no.nav.tiltakspenger.vedtak.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.UføreMottattHendelse
import java.time.LocalDateTime

data class UføreDTO(
    val ident: String,
    val journalpostId: String,
    val uføre: UforeResponsDTO,
    val innhentet: LocalDateTime,
)

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val uførepath = "/rivers/ufore"

fun Route.uføreRoutes(innsendingMediator: InnsendingMediator) {
    post("$uførepath") {
        LOG.info { "Vi har mottatt uførevedtak fra river" }
        val uføreDTO = call.receive<UføreDTO>()

        when {
            uføreDTO.uføre.feil != null -> {
                val feilMottattHendelse = FeilMottattHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = uføreDTO.journalpostId,
                    ident = uføreDTO.ident,
                    feil = when (uføreDTO.uføre.feil!!) {
                        Feilmelding.UkjentFeil -> Feil.UkjentFeil
                        Feilmelding.UgyldigIdent -> Feil.UgyldigIdent
                    },
                )
                innsendingMediator.håndter(feilMottattHendelse)
                call.respond(message = "OK", status = HttpStatusCode.OK)
            }

            uføreDTO.uføre.uføregrad != null -> {
                uføreDTO.uføre.uføregrad?.let { dto ->
                    val uføreMottattHendelse = UføreMottattHendelse(
                        aktivitetslogg = Aktivitetslogg(),
                        journalpostId = uføreDTO.journalpostId,
                        ident = uføreDTO.ident,
                        uføreVedtak = mapUføre(dto, uføreDTO.innhentet),
                        tidsstempelUføreVedtakInnhentet = uføreDTO.innhentet,
                    )
                    SECURELOG.info { " Mottatt uførevedtak og laget hendelse : $uføreMottattHendelse" }
                    innsendingMediator.håndter(uføreMottattHendelse)
                    call.respond(message = "OK", status = HttpStatusCode.OK)
                }
            }

            else ->
                throw IllegalStateException("Mottatt et uførevedtak som ikke har hverken uføregrad eller feil")
        }
    }
}

private fun mapUføre(uføregradDTO: UføregradDTO, innhentet: LocalDateTime): UføreVedtak {
    return UføreVedtak(
        id = UføreVedtakId.random(),
        harUføregrad = uføregradDTO.harUforegrad,
        datoUfør = uføregradDTO.datoUfor,
        virkDato = uføregradDTO.virkDato,
        innhentet = innhentet,
    )
}
