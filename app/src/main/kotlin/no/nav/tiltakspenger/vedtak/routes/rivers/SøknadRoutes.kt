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

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val søknadpath = "/rivers/soknad"

fun Route.søknadRoutes(
    innsendingMediator: InnsendingMediator,
    søkerMediator: SøkerMediator,
) {
    post("$søknadpath") {
        LOG.info { "Vi har mottatt søknad fra river" }
        val søknadDTO = call.receive<SøknadDTO>()

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

        val identMottattHendelse = IdentMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = søknadDTO.personopplysninger.ident,
        )
        søkerMediator.håndter(identMottattHendelse)

        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
