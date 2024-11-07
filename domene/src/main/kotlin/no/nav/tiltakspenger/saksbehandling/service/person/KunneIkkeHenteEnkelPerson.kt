package no.nav.tiltakspenger.saksbehandling.service.person

import no.nav.tiltakspenger.libs.common.Rolle

sealed interface KunneIkkeHenteEnkelPerson {
    data object FeilVedKallMotPdl : KunneIkkeHenteEnkelPerson
    data object FantIkkeSakId : KunneIkkeHenteEnkelPerson
    data class HarIkkeTilgang(
        val kreverEnAvRollene: List<Rolle>,
        val harRollene: List<Rolle>,
    ) : KunneIkkeHenteEnkelPerson
}
