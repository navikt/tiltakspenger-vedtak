package no.nav.tiltakspenger.felles

data class Saksbehandler(val navIdent: String, val brukernavn: String, val epost: String, val roller: List<Rolle>)
