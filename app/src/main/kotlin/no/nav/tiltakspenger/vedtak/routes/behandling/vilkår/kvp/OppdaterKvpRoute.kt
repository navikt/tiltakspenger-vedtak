package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.LeggTilKvpSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

/** Brukes ikke i MVPen. */
fun Route.oppdaterKvpRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    kvpVilkårService: KvpVilkårService,
) {
    data class DeltarForPeriode(
        val periode: PeriodeDTO,
        val deltar: Boolean,
    )

    data class Body(
        val ytelseForPeriode: List<DeltarForPeriode>,
        /** Drop-down i frontend. */
        val årsakTilEndring: ÅrsakTilEndringDTO,
    ) {
        fun toCommand(behandlingId: BehandlingId, saksbehandler: Saksbehandler): LeggTilKvpSaksopplysningCommand {
            return LeggTilKvpSaksopplysningCommand(
                deltakelseForPeriode = this.ytelseForPeriode.map {
                    LeggTilKvpSaksopplysningCommand.DeltakelseForPeriode(
                        periode = it.periode.toDomain(),
                        deltar = it.deltar,
                    )
                },
                årsakTilEndring = this.årsakTilEndring.toDomain(),
                behandlingId = behandlingId,
                saksbehandler = saksbehandler,
            )
        }
    }
    post("$behandlingPath/{behandlingId}/vilkar/kvp") {
        SECURELOG.debug("Mottatt request på $behandlingPath/{behandlingId}/vilkar/kvp")

        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))
        val body = call.receive<Body>()
        if (body.ytelseForPeriode.isEmpty()) {
            throw IllegalArgumentException("Dersom saksbehandler ønsker å legge til en kvp-saksopplysning må hen spesifisere minst én periode")
        }
        kvpVilkårService.leggTilSaksopplysning(
            body.toCommand(behandlingId, saksbehandler),
        ).let {
            call.respond(
                status = HttpStatusCode.Created,
                message = it.vilkårssett.kvpVilkår.toDTO(),
            )
        }
    }
}
