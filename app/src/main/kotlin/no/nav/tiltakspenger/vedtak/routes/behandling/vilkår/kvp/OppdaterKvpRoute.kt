package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.LeggTilKvpSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

/** Brukes ikke i MVPen. */
fun Route.oppdaterKvpRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    kvpVilkårService: KvpVilkårService,
    auditService: AuditService,
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
        fun toCommand(
            behandlingId: BehandlingId,
            saksbehandler: Saksbehandler,
            correlationId: CorrelationId,
        ): LeggTilKvpSaksopplysningCommand =
            LeggTilKvpSaksopplysningCommand(
                deltakelseForPeriode =
                this.ytelseForPeriode.map {
                    LeggTilKvpSaksopplysningCommand.DeltakelseForPeriode(
                        periode = it.periode.toDomain(),
                        deltar = it.deltar,
                    )
                },
                årsakTilEndring = this.årsakTilEndring.toDomain(),
                behandlingId = behandlingId,
                saksbehandler = saksbehandler,
                correlationId = correlationId,
            )
    }
    post("$BEHANDLING_PATH/{behandlingId}/vilkar/kvp") {
        sikkerlogg.debug("Mottatt request på $BEHANDLING_PATH/{behandlingId}/vilkar/kvp")

        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))
        val body = call.receive<Body>()
        if (body.ytelseForPeriode.isEmpty()) {
            throw IllegalArgumentException(
                "Dersom saksbehandler ønsker å legge til en kvp-saksopplysning må hen spesifisere minst én periode",
            )
        }
        kvpVilkårService
            .leggTilSaksopplysning(
                body.toCommand(behandlingId, saksbehandler, call.correlationId()),
            ).let {
                auditService.logMedBehandlingId(
                    behandlingId = behandlingId,
                    navIdent = saksbehandler.navIdent,
                    action = AuditLogEvent.Action.UPDATE,
                    contextMessage = "Oppdaterer data om vilkåret kvalifikasjonsprogrammet",
                    correlationId = call.correlationId(),
                )

                call.respond(
                    status = HttpStatusCode.Created,
                    message = it.vilkårssett.kvpVilkår.toDTO(),
                )
            }
    }
}
