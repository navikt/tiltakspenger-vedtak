package no.nav.tiltakspenger.vedtak.routes

object Standardfeil {
    fun fantIkkeFnr(): ErrorJson = ErrorJson(
        "Fant ikke fødselsnummer",
        "fant_ikke_fnr",
    )
}
