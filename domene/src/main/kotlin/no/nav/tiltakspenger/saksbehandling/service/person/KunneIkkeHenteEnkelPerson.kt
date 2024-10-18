package no.nav.tiltakspenger.saksbehandling.service.person

sealed interface KunneIkkeHenteEnkelPerson {
    object FeilVedKallMotPdl : KunneIkkeHenteEnkelPerson
    object FantIkkeSakId : KunneIkkeHenteEnkelPerson
}
