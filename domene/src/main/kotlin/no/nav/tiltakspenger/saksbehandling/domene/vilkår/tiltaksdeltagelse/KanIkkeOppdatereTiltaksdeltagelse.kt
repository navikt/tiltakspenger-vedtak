package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltaksdeltagelse

import no.nav.tiltakspenger.libs.common.Saksbehandlerrolle

sealed interface KanIkkeOppdatereTiltaksdeltagelse {
    data class HarIkkeTilgang(
        val kreverEnAvRollene: Set<Saksbehandlerrolle>,
        val harRollene: Set<Saksbehandlerrolle>,
    ) : KanIkkeOppdatereTiltaksdeltagelse
}
