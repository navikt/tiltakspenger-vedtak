package no.nav.tiltakspenger.vedtak

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.pipeline.PipelineContext
import no.nav.tiltakspenger.vedtak.exception.types.ManglerParameterIUrlException

object URLParameters {

    fun hentBehandlingId(context: PipelineContext<Unit, ApplicationCall>):  String {

        val behandlingId = context.call.parameters["behandlingId"] ?:
                            throw ManglerParameterIUrlException("Mangler behandlingId i URLen")
        return behandlingId
    }
}


