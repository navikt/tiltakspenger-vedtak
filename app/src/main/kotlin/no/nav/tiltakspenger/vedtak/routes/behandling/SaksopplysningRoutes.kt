package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.august
import no.nav.tiltakspenger.felles.mai
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

fun mapInngangsvilkårDTO() {
    InngangsvilkårDTO(
        listOf(
            TiltakDeltakelseDTO(
                deltakelser = listOf(
                    DeltakelseDTO(
                        periode = Periode(
                            fra = 1.mai(2024),
                            til = 15.august(2024),
                        ),
                        antallDager = 3,
                        deltakelse = "Deltar",
                    ),
                ),
                tiltaksvariant = "Gruppe AMO - Kjetils grill AS",
                status = "Deltar",
                tiltaksPeriode = Periode(fra = 1.mai(2024), til = 15.mai(2024)),
                harSøkt = true,
                girRett = true,
                kilde = Kilde.KOMET,
            ),
        ),
    )
}

fun Route.saksopplysningRoutes() {
    get("$behandlingPath/{behandlingId}/inngangsvilkår") {
        val dto = mapInngangsvilkårDTO()
        call.respond(status = HttpStatusCode.OK, dto)
    }
}
