package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårService
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.oppdaterLivsoppholdRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    livsoppholdVilkårService: LivsoppholdVilkårService,
) {
    data class YtelseForPeriode(
        val periode: PeriodeDTO,
        val harYtelse: Boolean,
    )

    data class Body(
        val ytelseForPeriode: YtelseForPeriode,
        /** Drop-down i frontend. */
        // TODO kew: Her skal vi ta inn ÅrsakTilEndring når det er på plass. Setter til null enn så lenge..
    ) {
        fun toCommand(behandlingId: BehandlingId, saksbehandler: Saksbehandler): LeggTilLivsoppholdSaksopplysningCommand {
            return LeggTilLivsoppholdSaksopplysningCommand(
                harYtelseForPeriode = LeggTilLivsoppholdSaksopplysningCommand.HarYtelseForPeriode(
                    periode = this.ytelseForPeriode.periode.toDomain(),
                    harYtelse = this.ytelseForPeriode.harYtelse,
                ),
                // TODO kew: Setter denne til null siden det ikke skal med i første omgang
                årsakTilEndring = null,
                behandlingId = behandlingId,
                saksbehandler = saksbehandler,
            )
        }
    }

    post("$behandlingPath/{behandlingId}/vilkar/livsopphold") {
        SECURELOG.debug("Mottatt request på $behandlingPath/{behandlingId}/vilkar/livsopphold")

        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))
        val body = call.receive<Body>()

        livsoppholdVilkårService.leggTilSaksopplysning(
            body.toCommand(behandlingId, saksbehandler),
        ).fold({
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = """
                    {
                        "feilmelding": "Perioden til saksopplysningen er forskjellig fra vurderingsperioden"
                    }
                """.trimIndent(),
            )
        }, {
            call.respond(
                status = HttpStatusCode.Created,
                message = it.vilkårssett.livsoppholdVilkår.toDTO(),
            )
        })
    }
}
