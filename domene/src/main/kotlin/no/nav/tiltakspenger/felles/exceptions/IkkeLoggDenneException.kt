package no.nav.tiltakspenger.felles.exceptions

class IkkeLoggDenneException(
    override val message: String,
) : RuntimeException(message)
