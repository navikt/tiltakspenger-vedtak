package no.nav.tiltakspenger.vedtak.routes.exceptionhandling

import io.ktor.http.HttpStatusCode

class ExceptionResponse(
    val status: Int,
    val title: String,
    val detail: String,
) {
    companion object {
        operator fun invoke(
            ex: Throwable,
            statusCode: HttpStatusCode,
        ) = ExceptionResponse(
            status = statusCode.value,
            title = ex::class.simpleName ?: "Ukjent feil",
            detail = ex.message ?: ex.toString(),
        )
    }
}
