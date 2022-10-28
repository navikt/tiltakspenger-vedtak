package no.nav.tiltakspenger.vedtak.routes.person

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.Søknad
import no.nav.tiltakspenger.domene.Tiltak
import no.nav.tiltakspenger.vedtak.audit.auditHvisInnlogget
import no.nav.tiltakspenger.vedtak.service.PersonService
import no.nav.tiltakspenger.vedtak.service.SøkerDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

private val LOG = KotlinLogging.logger {}

internal const val personPath = "/saker/person"

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

    route("$personPath") {
        get {
            LOG.info { "Vi har truffet GET /saker/person" }

            call.auditHvisInnlogget(berørtBruker = "person")

            LOG.info("Returnere dummydata")
            call.respond(message = dummyPerson(), status = HttpStatusCode.OK)
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

fun dummyPerson(): PersonDTO = PersonDTO(
    personopplysninger = PersonopplysningerDTO(
        fornavn = "Fornavn",
        etternavn = "Etternavn",
        ident = "123454",
        barn = listOf(
            BarnDTO(
                fornavn = "Emil",
                etternavn = "Flaks",
                ident = "987654",
                bosted = ""
            ),
            BarnDTO(
                fornavn = "Emma",
                etternavn = "Flaks",
                ident = "987655",
                bosted = ""
            )
        )
    ),
    behandlinger = listOf(
        BehandlingDTO(
            id = "behandlingId",
            søknad = Søknad(
                id = "søknadId",
                ident = "personIdent?",
                opprettet = LocalDateTime.of(2022, 5, 30, 20, 0, 0),
                tiltak = Tiltak(
                    id = "tiltakId",
                    arrangør = "arrangør",
                    navn = "navnTiltak",
                    startDato = LocalDate.of(2022, Month.APRIL, 30),
                    sluttDato = LocalDate.of(2022, Month.APRIL, 30),
                ),
                deltarKvp = false
            ),
            tiltak = TiltakDTO(
                arrangør = "Joblearn",
                navn = "Gruppe AMO",
                periode = PeriodeDTO(
                    fra = LocalDate.of(2022, Month.APRIL, 1),
                    til = LocalDate.of(2022, Month.APRIL, 20),
                ),
                prosent = 80,
                dagerIUken = 4,
                status = "Godkjent"
            ),
            vurderingsperiode = PeriodeDTO(
                fra = LocalDate.of(2022, Month.APRIL, 1),
                til = LocalDate.of(2022, Month.APRIL, 20),
            ),
            vurderinger = listOf(
                VilkårsVurderingsKategori(
                    tittel = "Statlige ytelser",
                    utfall = UtfallDTO.Uavklart,
                    vilkårsvurderinger = listOf(
                        VilkårsvurderingDTO(
                            utfall = UtfallDTO.Oppfylt,
                            periode = PeriodeDTO(
                                fra = LocalDate.of(2022, Month.APRIL, 1),
                                til = LocalDate.of(2022, Month.APRIL, 20),
                            ),
                            vilkår = "Dagpenger",
                            kilde = "Arena"
                        ),
                        VilkårsvurderingDTO(
                            utfall = UtfallDTO.Oppfylt,
                            periode = PeriodeDTO(
                                fra = LocalDate.of(2022, Month.APRIL, 1),
                                til = LocalDate.of(2022, Month.APRIL, 20),
                            ),
                            vilkår = "AAP",
                            kilde = "Arena"
                        ),
                        VilkårsvurderingDTO(
                            utfall = UtfallDTO.Uavklart,
                            periode = PeriodeDTO(
                                fra = LocalDate.of(2022, Month.APRIL, 1),
                                til = LocalDate.of(2022, Month.APRIL, 20),
                            ),
                            vilkår = "Tiltakspenger",
                            kilde = "Arena"
                        )
                    ),
                )
            )
        )
    ),
)
