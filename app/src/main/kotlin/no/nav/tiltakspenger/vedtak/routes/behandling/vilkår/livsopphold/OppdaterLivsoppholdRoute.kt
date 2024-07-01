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
        val ytelseForPeriode: List<YtelseForPeriode>,
        /** Drop-down i frontend. */
        val årsakTilEndring: ÅrsakTilEndringDto,
    ) {
        fun toCommand(
            behandlingId: BehandlingId,
            livsoppholdsytelse: LivsoppholdsytelseDto,
            saksbehandler: Saksbehandler,
        ): LeggTilLivsoppholdSaksopplysningCommand {
            return LeggTilLivsoppholdSaksopplysningCommand(
                ytelseForPeriode = this.ytelseForPeriode.map {
                    LeggTilLivsoppholdSaksopplysningCommand.YtelseForPeriode(
                        periode = it.periode.toDomain(),
                        harYtelse = it.harYtelse,
                    )
                },
                årsakTilEndring = this.årsakTilEndring.toDomain(),
                livsoppholdsytelse = livsoppholdsytelse.toDomain(),
                behandlingId = behandlingId,
                saksbehandler = saksbehandler,
            )
        }
    }
    post("$behandlingPath/{behandlingId}/vilkar/livsopphold//{ytelse}") {
        SECURELOG.debug("Mottatt request på $behandlingPath/{behandlingId}/vilkar/livsopphold")

        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))
        val ytelse = LivsoppholdsytelseDto.valueOf(call.parameter("ytelse"))
        val body = call.receive<Body>()
        if (body.ytelseForPeriode.isEmpty()) {
            throw IllegalArgumentException("Dersom saksbehandler ønsker å legge til en livsopphold-saksopplysning må hen spesifisere minst én periode")
        }
        livsoppholdVilkårService.leggTilSaksopplysning(
            body.toCommand(behandlingId, ytelse, saksbehandler),
        ).let {
            call.respond(
                status = HttpStatusCode.Created,
                message = it.vilkårssett.livsoppholdVilkår.toDTO(),
            )
        }
    }
}
