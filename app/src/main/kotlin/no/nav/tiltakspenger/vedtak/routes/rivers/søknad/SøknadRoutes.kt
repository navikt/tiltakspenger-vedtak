package no.nav.tiltakspenger.vedtak.routes.rivers.søknad

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.meldinger.IdentMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.innsending.service.ports.InnsendingMediator
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.SøkerMediator

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
const val søknadpath = "/rivers/soknad"

fun Route.søknadRoutes(
    innsendingMediator: InnsendingMediator,
    søkerMediator: SøkerMediator,
    sakService: SakService,
) {
    post(søknadpath) {
        LOG.info { "Vi har mottatt søknad fra river" }
        val søknadDTO = call.receive<SøknadDTO>()

        // Oppretter sak med søknad og lagrer den
        sakService.motta(
            søknad = SøknadDTOMapper.mapSøknad(
                dto = søknadDTO,
                innhentet = søknadDTO.opprettet,
            ),
        )

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

        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
