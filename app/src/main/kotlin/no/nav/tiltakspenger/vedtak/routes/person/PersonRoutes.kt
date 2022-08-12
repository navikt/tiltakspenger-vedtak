package no.nav.tiltakspenger.vedtak.routes.person

import com.papsign.ktor.openapigen.route.path.auth.OpenAPIAuthenticatedRoute
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.server.auth.jwt.*
import no.nav.tiltakspenger.domene.Søknad
import no.nav.tiltakspenger.domene.Tiltak
import no.nav.tiltakspenger.vedtak.modell.BarnDTO
import no.nav.tiltakspenger.vedtak.modell.BehandlingDTO
import no.nav.tiltakspenger.vedtak.modell.PeriodeDTO
import no.nav.tiltakspenger.vedtak.modell.PersonDTO
import no.nav.tiltakspenger.vedtak.modell.PersonaliaDTO
import no.nav.tiltakspenger.vedtak.modell.TiltakDTO
import no.nav.tiltakspenger.vedtak.modell.UtfallDTO
import no.nav.tiltakspenger.vedtak.modell.VilkårsVurderingsKategori
import no.nav.tiltakspenger.vedtak.modell.VilkårsvurderingDTO
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

fun OpenAPIAuthenticatedRoute<JWTPrincipal>.personRoutes() {
    route("$personPath/test") {
        get<Unit, PersonDTO, JWTPrincipal> {
            respond(response = person())
        }
    }
}

internal const val personPath = "/person"

fun person(): PersonDTO = PersonDTO(
    personalia = PersonaliaDTO(
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

