package no.nav.tiltakspenger.vedtak.routes.exceptionhandling

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.uri
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.vedtak.exceptions.ManglendeJWTTokenException
import no.nav.tiltakspenger.vedtak.exceptions.UgyldigRequestException
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.Standardfeil.ikkeAutorisert
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.Standardfeil.ikkeFunnet
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.Standardfeil.ikkeTilgang
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.Standardfeil.serverfeil
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.Standardfeil.ugyldigRequest

object ExceptionHandler {
    private val logger = KotlinLogging.logger {}
    suspend fun handle(
        call: ApplicationCall,
        cause: Throwable,
    ) {
        val uri = call.request.uri
        logger.error(RuntimeException("Trigger stacktrace for enklere debug.")) { "Ktor mottok exception i ytterste lag. Uri: $uri. Se sikkerlogg mer kontekst." }
        sikkerlogg.error(cause) { "Ktor mottok exception i ytterste lag. Uri: $uri." }
        when (cause) {
            is IllegalStateException -> {
                call.respond500InternalServerError(serverfeil())
            }

            is ManglendeJWTTokenException -> {
                call.respond401Unauthorized(ikkeAutorisert())
            }

            is UgyldigRequestException -> {
                call.respond400BadRequest(ugyldigRequest())
            }

            is ContentTransformationException -> {
                call.respond400BadRequest(ugyldigRequest())
            }

            is TilgangException -> {
                call.respond403Forbidden(ikkeTilgang())
            }

            is IkkeFunnetException -> {
                call.respond404NotFound(ikkeFunnet())
            }

            // Catch all
            else -> {
                call.respond500InternalServerError(serverfeil())
            }
        }
    }
}
