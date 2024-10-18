package no.nav.tiltakspenger.vedtak.routes.exceptionhandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.vedtak.exceptions.ManglendeJWTTokenException
import no.nav.tiltakspenger.vedtak.exceptions.UgyldigRequestException

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
                call.respondWith(HttpStatusCode.InternalServerError, cause)
            }

            is ManglendeJWTTokenException -> {
                call.respondWith(HttpStatusCode.Unauthorized, cause)
            }

            is UgyldigRequestException -> {
                call.respondWith(HttpStatusCode.BadRequest, cause)
            }

            is ContentTransformationException -> {
                call.respondWith(HttpStatusCode.BadRequest, cause)
            }

            is TilgangException -> {
                call.respondWith(HttpStatusCode.Forbidden, cause)
            }

            is IkkeFunnetException -> {
                call.respondWith(HttpStatusCode.NotFound, cause)
            }

            // Catch all
            else -> {
                call.respondWith(HttpStatusCode.InternalServerError, cause)
            }
        }
    }

    // TODO pre-mvp jah: Føles ikke bra at vi returnerer cause fra exceptions. Her kan vi blø data vi ikke ønsker å dele.
    private suspend fun ApplicationCall.respondWith(
        statusCode: HttpStatusCode,
        ex: Throwable,
    ) {
        this.respond(
            statusCode,
            ExceptionResponse(ex, statusCode),
        )
    }
}
