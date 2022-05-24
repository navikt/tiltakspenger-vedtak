package no.nav.tiltakspenger.vedtak.routes.sak

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal const val sakPath = "/saker"

internal fun Route.sakRoutes(

) {
    get("$sakPath/person") {
        call.respondText(
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.OK,
            text =
            // language=JSON
            """{
                "sak" :
                    {
                        "id" : "4d4g54g5-5g5g44g-4jh5jh",
                        "tiltak" : {
                            "test" : "Er info om tiltak en egen ting her, eller ligger det i behandling?",
                            "tiltak" : "Et kult tiltak",
                            "dager" : "Kanskje hvilke dager det er forventet at bruker møter opp?"
                        },
                        "behandlinger" : [
                            {
                                "id" : "45jhg645jh-4k5jh6-kj54h6",
                                "type" : "Førstegangsbehandling",
                                "status" : "Iverksatt",
                                "vilkårsvurderinger" : [ 
                                {
                                    "periode" : { "fra" : "2022-05-01", "til" : "2022-05-31"},
                                    "vilkårsvurderinger" : [
                                        {
                                            "vilkår" : { "erInngangsVilkår" : true, "paragraf" : "PARAGRAF_3_LEDD_1_PUNKTUM1"},
                                            "fakta" : [ { "Faktum" : { "kilde" : "BRUKER", "deltarKVP" :  false} }],
                                            "vurderingsperiode" : { "fra" : "2022-05-01", "til" : "2022-05-31"},
                                            "utfallsperioder" : [ { "utfall" : "IkkeVurdert", "periode" : { "fra" : "2022-05-01", "til" : "2022-05-31"} } ]
                                        },
                                        {
                                            "vilkår" : { "erInngangsVilkår" : true, "paragraf" : "PARAGRAF_3_LEDD_1_PUNKTUM1"},
                                            "fakta" : [ { "Faktum" : "BRUKER" }],
                                            "vurderingsperiode" : { "fra" : "2022-05-01", "til" : "2022-05-31"},
                                            "utfallsperioder" : [ { "utfall" : "IkkeVurdert", "periode" : { "fra" : "2022-05-01", "til" : "2022-05-31"} } ]
                                        }
                                    ]
                                }
                                ]
                            }
                        ],
                        "vedtak" : [
                                {
                                    "id" : "435kjh34-kj43h5-k3j4h5"
                                }
                            ],
                        "meldekort" : "Skal meldekort ligge på dette nivået?",
                        "oppmøte" : "Dette nivået? Skal dette være en felles ting med meldekort?",
                        "utbetalinger" : [
                            {
                                "test" : "Denne må kanskje et nivå lengre ut hvis den skal gjelde alle sakene?",
                                "id" : "345jhg-45g5-h45h45hy-5ht",
                                "periode" : { "fra" : "2022-05-01", "til" : "2022-05-31"},
                                "beløp" : 3000
                            }
                        ]
                    }
                }""".trimMargin()
        )
    }
}