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

    fun fantIkkeMeldekort(): ErrorJson = ErrorJson(
        "Fant ikke meldekort",
        "fant_ikke_meldekort",
    )

    fun fantIkkeTiltak(): ErrorJson = ErrorJson(
        "Fant ikke igjen tiltaket det er søkt på",
        "fant_ikke_tiltak",
    )

    fun måVæreBeslutter(): ErrorJson = ErrorJson(
        "Må ha beslutter-rolle.",
        "må_ha_beslutter_rolle",
    )

    fun støtterIkkeBarnetillegg(): ErrorJson = ErrorJson(
        "Vi støtter ikke barnetillegg.",
        "støtter_ikke_barnetillegg",
    )

    fun støtterIkkeDelvisEllerAvslag(): ErrorJson = ErrorJson(
        "Vi støtter ikke delvis innvilgelse eller avslag.",
        "støtter_ikke_delvis_innvilgelse_eller_avslag",
    )

    fun saksbehandlerOgBeslutterKanIkkeVæreLik(): ErrorJson = ErrorJson(
        "Beslutter kan ikke være den samme som saksbehandler.",
        "beslutter_og_saksbehandler_kan_ikke_være_lik",
    )
}
