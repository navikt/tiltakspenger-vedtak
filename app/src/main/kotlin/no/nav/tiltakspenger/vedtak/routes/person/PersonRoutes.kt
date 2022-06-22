package no.nav.tiltakspenger.vedtak.routes.person

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Søknad
import no.nav.tiltakspenger.domene.Tiltak
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import kotlin.random.Random

internal const val personPath = "/person"

fun Route.personRoutes() {
    get("$personPath/test") {
        val person = PersonDTO(
            personalia = PersonaliaDTO(
                fornavn = "Fornavn",
                etternavn = "Etternavn",
                ident = "123454",
                barn = listOf(
                    BarnDTO(
                        fornavn = "FornavnBarn",
                        etternavn = "EtternavnBarn",
                        ident = "987654"
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
        call.respond(person)
        /*
        call.respondText(
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.OK,
            text = serialize(person)
        )*/
    }

    get("$personPath") {
        call.respondText(
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.OK,
            text =
            // language=JSON
            """
                {
                  "isRefreshing": ${Random.nextBoolean()},
                  "saker": [{
                      "id" : "45jhg645jh-4k5jh6-kj54h6",
                      "saksPeriode": { "fra": "12-12-22", "til": "01-02-23" },
                      "behandlinger": [
                         {
                            "type" : "Førstegangsbehandling",
                            "status" : "Iverksatt",
                            "periode" : { "fra" : "2022-05-01", "til" : "2022-05-31"},
                            "søknad": {
                                "id": "12312-12313-1231",
                                "vedlegg": [],
                                "søknadsdato": "asds",
                                "fra": "asda",
                                "til": "asdas",
                                "tiltak" : {
                                    "test" : "Er info om tiltak en egen ting her, eller ligger det i behandling?",
                                    "tiltak" : "Et kult tiltak",
                                    "dager" : "Kanskje hvilke dager det er forventet at bruker møter opp?",
                                    "periode": { "fra": "11-12-12", "til": "11-12-12" }
                                }
                             },
                            "vilkårsvurderinger" : [
                                {
                                  "kategori": "Institusjonsopphold",
                                  "utfall": "IkkeOppfylt",
                                  "vurderinger": [
                                    {
                                      "utfall": "IkkeOppfylt",
                                      "vilkår": "Opphold på institusjon",
                                      "periode": { "fra": "11-12-12", "til": "11-12-12" },
                                      "kilde": "Inst2"
                                    },
                                    {
                                      "utfall": "Oppfylt",
                                      "vilkår": "Opphold på institusjon",
                                      "periode": { "fra": "11-12-12", "til": "11-12-12" },
                                      "kilde": "Bruker"
                                    }
                                  ]
                                },
                                {
                                  "kategori": "Statlige ytelser",
                                  "utfall": "Oppfylt",
                                  "vurderinger": [
                                    {
                                      "utfall": "Oppfylt",
                                      "vilkår": "Ikke Dagpenger",
                                      "periode": { "fra": "11-12-12", "til": "11-12-12" },
                                      "kilde": "Arena"
                                    },
                                    {
                                      "utfall": "Oppfylt",
                                      "vilkår": "Ikke AAP",
                                      "periode": { "fra": "11-12-12", "til": "11-12-12" },
                                      "kilde": "Arena"
                                    }
                                  ]
                                }
                            ]
                         }
                      
                  }],
                  "vedtak" : [
                    {
                        "id" : "435kjh34-kj43h5-k3j4h5",
                        "utfall": "Avslag",
                        "søknadsId": "asdsa"
                    }
                  ],
                  "utbetalinger" : [
                    {
                        "test" : "Denne må kanskje et nivå lengre ut hvis den skal gjelde alle sakene?",
                        "id" : "345jhg-45g5-h45h45hy-5ht",
                        "periode" : { "fra" : "2022-05-01", "til" : "2022-05-31"},
                        "beløp" : 3000
                    }
                  ],
                  "meldekort" : "Skal meldekort ligge på dette nivået?",
                  "oppmøte" : "Dette nivået? Skal dette være en felles ting med meldekort?",
                  "personalia": {
                    "fornavn": "asds",
                    "etter": "asds",
                    "ident": "12312312312",
                    "barn": [
                      { "fornavn": "Emma", "etternavn": "Flaks", "fødselsdato": "11.12.12", "bosatt": "Norge" }
                    ]
                  }
                }
            """.trimMargin()
        )
    }
}
