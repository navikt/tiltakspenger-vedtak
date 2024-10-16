package no.nav.tiltakspenger.vedtak.routes

object Standardfeil {

    fun fantIkkeFnr(): ErrorJson = ErrorJson(
        "Fant ikke fødselsnummer",
        "fant_ikke_fnr",
    )

    fun fantIkkeSak(): ErrorJson = ErrorJson(
        "Fant ikke sak",
        "fant_ikke_sak",
    )

    fun måVæreBeslutter(): ErrorJson = ErrorJson(
        "Må ha beslutter-rolle.",
        "må_ha_beslutter_rolle",
    )

    fun saksbehandlerOgBeslutterKanIkkeVæreLik(): ErrorJson = ErrorJson(
        "Beslutter kan ikke være den samme som saksbehandler.",
        "beslutter_og_saksbehandler_kan_ikke_være_lik",
    )
}
