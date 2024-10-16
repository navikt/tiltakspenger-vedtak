package no.nav.tiltakspenger.felles.exceptions

class StøtterIkkeUtfallException(
    override val message: String,
) : RuntimeException(message)
