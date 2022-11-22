package no.nav.tiltakspenger.vedtak.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*

suspend fun ApplicationTestBuilder.defaultRequest(
    method: HttpMethod,
    uri: String,
    setup: HttpRequestBuilder.() -> Unit = {},
): HttpResponse {
    return this.client.request(uri) {
        this.method = method
        this.headers {
            append(HttpHeaders.XCorrelationId, "DEFAULT_CALL_ID")
            append(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        setup()
    }
}
