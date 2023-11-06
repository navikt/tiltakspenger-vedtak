package no.nav.tiltakspenger.vedtak.exception

import kotlinx.serialization.Serializable

@Serializable
class ExceptionResponse(
    val message: String,
    val code: Int
)
