package no.nav.tiltakspenger.vedtak.routes.exceptionhandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

object ExceptionHandler {

    suspend fun handle(
        call: ApplicationCall,
        cause: Throwable,
    ) {
        when (cause) {
            is IllegalStateException -> {
                call.respondWith(HttpStatusCode.InternalServerError, cause)
            }

            is ManglendeJWTTokenException -> {
                call.respondWith(HttpStatusCode.Unauthorized, cause)
            }

            // Catch all
            else -> {
                call.respondWith(HttpStatusCode.InternalServerError, cause)
            }
        }
    }

    private suspend fun ApplicationCall.respondWith(statusCode: HttpStatusCode, ex: Throwable) {
        this.respond(
            statusCode,
            ExceptionResponse(ex, statusCode),
        )
    }
}
