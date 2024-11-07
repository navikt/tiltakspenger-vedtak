package no.nav.tiltakspenger.saksbehandling.service.sak

import no.nav.tiltakspenger.libs.common.Rolle

sealed interface KunneIkkeHenteSakForFnr {
    data object FantIkkeSakForFnr : KunneIkkeHenteSakForFnr
    data class HarIkkeTilgang(
        val kreverEnAvRollene: List<Rolle>,
        val harRollene: List<Rolle>,
    ) : KunneIkkeHenteSakForFnr
}
