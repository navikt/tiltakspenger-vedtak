package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.leggSammen
import no.nav.tiltakspenger.domene.periode
import no.nav.tiltakspenger.domene.trekkFra

fun List<Vurdering>.konklusjonFor(vurderingsperiode: Periode): Konklusjon {

    val ikkeOppfyltVurderinger = this.filter { it.utfall == Utfall.IKKE_OPPFYLT }
    val manuelleVurderinger = this.filter { it.utfall == Utfall.KREVER_MANUELL_VURDERING }
    val oppfyllteVurderinger = this.filter { it.utfall == Utfall.OPPFYLT }

    if (this.all { it.utfall == Utfall.OPPFYLT }) {
        return Konklusjon.Oppfylt(vurderingsperiode to this.map { it.vilkår }.toSet())
    }

    val ikkeOppfyltePerioder = ikkeOppfyltVurderinger.map { it.periode() }.leggSammen()

    if (ikkeOppfyltePerioder.any { it.inneholderHele(vurderingsperiode) }) {
        return Konklusjon.IkkeOppfylt(vurderingsperiode to ikkeOppfyltVurderinger.map { it.vilkår }.toSet())
    }

    val manuellePerioder = manuelleVurderinger.map { it.periode() }.leggSammen().trekkFra(ikkeOppfyltePerioder)

    if (manuellePerioder.any { it.overlapperMed(vurderingsperiode) }) {
        val resultat: Map<Periode, Set<Vilkår>> = manuellePerioder
            .filter { it.overlapperMed(vurderingsperiode) }
            .associate { mp ->
                mp.overlappendePeriode(vurderingsperiode)!! to manuelleVurderinger
                    .filter { it.periode().overlapperMed(mp) }
                    .map { it.vilkår }.toSet()
            }
        return Konklusjon.KreverManuellBehandling(resultat)
    }

    val oppfyltePerioder: List<Periode> = vurderingsperiode.trekkFra(ikkeOppfyltePerioder)

    val oppfylte: List<Konklusjon.Oppfylt> = oppfyltePerioder
        .mapNotNull { it.overlappendePeriode(vurderingsperiode) }
        .map { periode ->
            Konklusjon.Oppfylt(periode to oppfyllteVurderinger.map { vurdering -> vurdering.vilkår }.toSet())
        }
    val ikkeOppfylte: List<Konklusjon.IkkeOppfylt> = ikkeOppfyltePerioder
        .mapNotNull { it.overlappendePeriode(vurderingsperiode) }
        .map { periode ->
            Konklusjon.IkkeOppfylt(
                //Denne er ikke korrekt, må bare ta med de som faktisk gjelder den aktuelle perioden
                periode to ikkeOppfyltVurderinger.map { vurdering -> vurdering.vilkår }.toSet()
            )
        }
    return Konklusjon.DelvisOppfylt(oppfylte, ikkeOppfylte)
}

sealed interface Konklusjon {

    data class Oppfylt(val periodeMedVilkår: Pair<Periode, Set<Vilkår>>) : Konklusjon

    data class IkkeOppfylt(val periodeMedVilkår: Pair<Periode, Set<Vilkår>>) : Konklusjon

    data class KreverManuellBehandling(val perioderMedVilkår: Map<Periode, Set<Vilkår>>) : Konklusjon

    data class DelvisOppfylt(val oppfylt: List<Oppfylt>, val ikkeOppfylt: List<IkkeOppfylt>) : Konklusjon
}
