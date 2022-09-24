package no.nav.tiltakspenger.vedtak.routes.person

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.Søknad
import no.nav.tiltakspenger.domene.Tiltak
import no.nav.tiltakspenger.vedtak.audit.auditHvisInnlogget
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

private val LOG = KotlinLogging.logger {}

internal const val personPath = "/saker/person"

fun Route.personRoutes(innloggetBrukerProvider: InnloggetBrukerProvider) {
    route("$personPath") {
        route("/test") {
            get {
                call.auditHvisInnlogget(berørtBruker = "test")
                LOG.info { "Vi har truffet /saker/person/test" }
                call.respond(message = person(), status = HttpStatusCode.OK)
            }
        }
        get {
            if (call.request.queryParameters["ident"] == "asc") {
                // Show products from the lowest price to the highest
            }
            call.auditHvisInnlogget(berørtBruker = "person")
            LOG.info { "Vi har truffet /saker/person" }
            LOG.info { "Vi har tenkt til å sende tilbake ${person()} " }
            call.respond(message = person(), status = HttpStatusCode.OK)
        }
    }
}

fun person(): PersonDTO = PersonDTO(
    personopplysninger = PersonopplysningerDTO(
        fornavn = "Fornavn",
        etternavn = "Etternavn",
        ident = "123454",
        barn = listOf(
            BarnDTO(
                fornavn = "Emil",
                etternavn = "Flaks",
                ident = "987654",
                bosted = "NORGE"
            ),
            BarnDTO(
                fornavn = "Emma",
                etternavn = "Flaks",
                ident = "987655",
                bosted = "NORGE"
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
            periode = PeriodeDTO(
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
