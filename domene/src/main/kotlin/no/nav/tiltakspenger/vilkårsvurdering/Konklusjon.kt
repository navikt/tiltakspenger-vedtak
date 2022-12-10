package no.nav.tiltakspenger.vilkårsvurdering

import com.google.common.collect.Range
import com.google.common.collect.TreeRangeSet
import no.nav.tiltakspenger.domene.LocalDateDiscreteDomain
import no.nav.tiltakspenger.domene.Periode
import java.time.LocalDate

fun List<Vurdering>.konklusjonFor(vurderingsperiode: Periode): Konklusjon {
    val rangeSet = TreeRangeSet.create<LocalDate>()
    val ikkeOppfyltVilkår = this.filter {
        it.utfall == Utfall.IKKE_OPPFYLT
    }
    ikkeOppfyltVilkår.map {
        Range.closed(it.fom!!, it.tom!!).canonical(LocalDateDiscreteDomain())
    }.forEach{
        rangeSet.add(it)
    }
    if (rangeSet.encloses(vurderingsperiode.range)) {
        return Konklusjon.IkkeOppfylt(vurderingsperiode to ikkeOppfyltVilkår.map { it.vilkår }.toSet() )
    }

    return TODO()
}

sealed interface Konklusjon {

    data class Oppfylt(val periodeMedVilkår: Pair<Periode, Set<Vilkår>>) : Konklusjon

    data class IkkeOppfylt(val periodeMedVilkår: Pair<Periode, Set<Vilkår>>) : Konklusjon

    data class KreverManuellBehandling(val perioderMedVilkår: Map<Periode, Set<Vilkår>>) : Konklusjon

    data class DelvisOppfylt(val oppfylt: List<Oppfylt>, val ikkeOppfylt: List<IkkeOppfylt>)
}
