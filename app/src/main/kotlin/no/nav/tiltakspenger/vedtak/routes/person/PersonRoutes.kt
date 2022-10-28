package no.nav.tiltakspenger.vedtak.routes.person

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.audit.auditHvisInnlogget
import no.nav.tiltakspenger.vedtak.service.PersonService
import no.nav.tiltakspenger.vedtak.service.SøkerDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider

private val LOG = KotlinLogging.logger {}

internal const val søknadPath = "/søknad"

internal const val søknaderPath = "/person/søknader"

data class PersonIdent(
    val ident: String
)

fun Route.personRoutes(
    innloggetBrukerProvider: InnloggetBrukerProvider,
    personService: PersonService,
) {
//    route(personPath) {
//        post {
//            val personIdent = deserialize<PersonIdent>(call.receive())
//
//            call.auditHvisInnlogget(berørtBruker = "person")
//
//            if (personIdent.ident != null) {
//                val person = personService.hentPerson(personIdent.ident)
//                if (person == null) {
//                    call.respond(message = "Vi fant ikke søker", status = HttpStatusCode.NotFound)
//                } else {
//                    LOG.info { "Vi har tenkt til å sende tilbake $person " }
//                    call.respond(message = person, status = HttpStatusCode.OK)
//                }
//            } else {
//                call.respond(message = "Vi trenger en ident", status = HttpStatusCode.NotFound)
//            }
//        }
//    }

    route("$søknadPath") {
        get {
            val søknadId = call.parameters["soknadId"]
            LOG.info { "Vi har truffet GET /søknad" }

            if (søknadId == null) {
                call.respond(message = "Mangler soknadId", status = HttpStatusCode.BadRequest)
            } else {
                val behandlingAvSøknad = personService.hentBehandlingAvSøknad(søknadId)
                if (behandlingAvSøknad == null) {
                    call.respond(message = "Søknad ikke funnet", status = HttpStatusCode.NotFound)
                } else {
                    call.auditHvisInnlogget(berørtBruker = behandlingAvSøknad.personopplysninger.ident)
                    call.respond(message = behandlingAvSøknad, status = HttpStatusCode.OK)
                }
            }
        }
    }

    route(søknaderPath) {
        post {
            val personIdent = call.receive<PersonIdent>()
            call.auditHvisInnlogget(berørtBruker = personIdent.ident)

            val response: SøkerDTO? = personService.hentSøkerOgSøknader(personIdent.ident)
            if (response == null) {
                call.respond(message = "Søker ikke funnet", status = HttpStatusCode.NotFound)
            } else {
                call.respond(message = response, status = HttpStatusCode.OK)
            }
        }
    }
}
