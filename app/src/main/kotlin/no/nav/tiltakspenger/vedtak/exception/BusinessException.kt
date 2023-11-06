package no.nav.tiltakspenger.vedtak.exception

open class BusinessException(val messageCode: MessageCode, message: String?) : RuntimeException(message)
