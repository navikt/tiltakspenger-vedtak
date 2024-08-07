package no.nav.tiltakspenger.felles

enum class Rolle {
    SAKSBEHANDLER,
    FORTROLIG_ADRESSE,
    STRENGT_FORTROLIG_ADRESSE,
    SKJERMING,
    LAGE_HENDELSER,

    // Systemadministrator (oss)
    DRIFT,
    BESLUTTER,

    // Saksbehandlers administrator (superbruker)
    ADMINISTRATOR,
}
