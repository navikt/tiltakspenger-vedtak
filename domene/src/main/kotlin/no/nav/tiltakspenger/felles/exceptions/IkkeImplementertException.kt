package no.nav.tiltakspenger.felles.exceptions

class IkkeImplementertException(
    override val message: String,
) : RuntimeException(message)
