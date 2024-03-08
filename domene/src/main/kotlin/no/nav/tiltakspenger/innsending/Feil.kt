package no.nav.tiltakspenger.innsending

enum class Feil(val message: String) {
    PersonIkkeFunnet("Fant ikke person i PDL"),
    IdentIkkeFunnet("Fant ikke person i Skjerming"),
    UkjentFeil("Ukjent feil i FP"),
    UgyldigIdent("Ugyldig ident i Pesys"),
    IkkeHentet("Overgangssstønad ikke hentet"),
    Feilet("Overgangssstønad feilet"),
    IkkeTilgang("Overgangssstønad ikke tilgang"),
    FunksjonellFeil("Overgangssstønad funksjonell feil"),
}
