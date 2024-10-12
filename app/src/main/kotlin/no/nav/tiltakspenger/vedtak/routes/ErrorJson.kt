package no.nav.tiltakspenger.vedtak.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

data class ErrorJson(
    val melding: String,
    val kode: String,
)

suspend fun ApplicationCall.respond403Forbidden(melding: String, kode: String) {
    this.respondError(
        status = HttpStatusCode.Forbidden,
        melding = melding,
        kode = kode,
    )
}

suspend fun ApplicationCall.respond401Unauthorized(melding: String, kode: String) {
    this.respondError(
        status = HttpStatusCode.Unauthorized,
        melding = melding,
        kode = kode,
    )
}
suspend fun ApplicationCall.respond500InternalServerError(melding: String, kode: String) {
    this.respondError(
        status = HttpStatusCode.InternalServerError,
        melding = melding,
        kode = kode,
    )
}
suspend fun ApplicationCall.respond400BadRequest(melding: String, kode: String) {
    this.respondError(
        status = HttpStatusCode.BadRequest,
        melding = melding,
        kode = kode,
    )
}

suspend fun ApplicationCall.respond404NotFound(melding: String, kode: String) {
    this.respondError(
        status = HttpStatusCode.NotFound,
        melding = melding,
        kode = kode,
    )
}

suspend fun ApplicationCall.respondError(status: HttpStatusCode, melding: String, kode: String) {
    this.respond(
        message = ErrorJson(
            melding = melding,
            kode = kode,
        ),
        status = status,
    )
}
