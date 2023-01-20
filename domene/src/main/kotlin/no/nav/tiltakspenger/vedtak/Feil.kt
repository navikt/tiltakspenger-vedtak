package no.nav.tiltakspenger.vedtak

enum class Feil(val message: String) {
    PersonIkkeFunnet("Fant ikke person i PDL"),
    IdentIkkeFunnet("Fant ikke person i Skjerming"),
}
