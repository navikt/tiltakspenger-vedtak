package no.nav.tiltakspenger.saksbehandling.service.sak

import no.nav.tiltakspenger.libs.common.Rolle

sealed interface KunneIkkeHenteSakForSaksnummer {
    data class HarIkkeTilgang(
        val kreverEnAvRollene: List<Rolle>,
        val harRollene: List<Rolle>,
    ) : KunneIkkeHenteSakForSaksnummer
}
