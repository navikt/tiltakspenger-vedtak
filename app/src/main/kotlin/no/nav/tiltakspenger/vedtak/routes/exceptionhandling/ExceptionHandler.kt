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
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ExceptionResponse(cause.message ?: cause.toString()),
                )
            }

            is ManglendeJWTTokenException -> {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ExceptionResponse(cause.message),
                )
            }

            // Catch all
            else -> {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ExceptionResponse(cause.message ?: cause.toString()),
                )
            }
        }
    }
}
