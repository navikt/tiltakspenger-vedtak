package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.meldinger.IdentMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.rivers.SøknadDTO
import no.nav.tiltakspenger.vedtak.rivers.SøknadDTOMapper
import no.nav.tiltakspenger.vedtak.service.sak.SakService

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val søknadpath = "/rivers/soknad"

fun Route.søknadRoutes(
    innsendingMediator: InnsendingMediator,
    søkerMediator: SøkerMediator,
    sakService: SakService,
) {
    post("$søknadpath") {
        LOG.info { "Vi har mottatt søknad fra river" }
        val søknadDTO = call.receive<SøknadDTO>()

        // Lager hendelse og trigger Innending innhenting
        val søknadMottattHendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = søknadDTO.dokInfo.journalpostId,
            søknad = SøknadDTOMapper.mapSøknad(
                dto = søknadDTO,
                innhentet = søknadDTO.opprettet,
            ),
        )

        SECURELOG.info { " Mottatt søknad og laget hendelse : $søknadMottattHendelse" }
        innsendingMediator.håndter(søknadMottattHendelse)

        // Lager hendelse og trigger oppretelse av Søker hvis den ikke finnes
        val identMottattHendelse = IdentMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = søknadDTO.personopplysninger.ident,
        )
        søkerMediator.håndter(identMottattHendelse)

        // Oppretter sak med søknad og lagrer den
        sakService.motta(
            søknad = SøknadDTOMapper.mapSøknad(
                dto = søknadDTO,
                innhentet = søknadDTO.opprettet,
            ),
        )

        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
