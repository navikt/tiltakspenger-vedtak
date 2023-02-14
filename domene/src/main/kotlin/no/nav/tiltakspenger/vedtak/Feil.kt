package no.nav.tiltakspenger.vedtak

enum class Feil(val message: String) {
    PersonIkkeFunnet("Fant ikke person i PDL"),
    IdentIkkeFunnet("Fant ikke person i Skjerming"),
    UkjentFeil("Ukjent feil i FP"),
    UgyldigIdent("Ugyldig ident i Pesys"),
}
