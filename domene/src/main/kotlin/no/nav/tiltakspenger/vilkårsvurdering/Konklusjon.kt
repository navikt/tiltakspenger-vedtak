package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode

fun List<Vurdering>.konklusjonFor(vurderingsperiode: Periode): Konklusjon {
    return TODO()
}

sealed interface Konklusjon {

    data class Oppfylt(val periodeMedVilkår: Pair<Periode, Set<Vilkår>>) : Konklusjon

    data class IkkeOppfylt(val periodeMedVilkår: Pair<Periode, Set<Vilkår>>) : Konklusjon

    data class KreverManuellBehandling(val perioderMedVilkår: Map<Periode, Set<Vilkår>>) : Konklusjon

    data class DelvisOppfylt(val oppfylt: List<Oppfylt>, val ikkeOppfylt: List<IkkeOppfylt>)
}
