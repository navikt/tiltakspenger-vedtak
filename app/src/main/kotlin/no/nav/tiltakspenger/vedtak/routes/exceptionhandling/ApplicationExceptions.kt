package no.nav.tiltakspenger.vedtak.routes.exceptionhandling

class ManglendeJWTTokenException(override val message: String = "JWTToken ikke funnet") : RuntimeException(message)

class UgyldigRequestException(override val message: String = "Ugyldig request") : RuntimeException(message)
