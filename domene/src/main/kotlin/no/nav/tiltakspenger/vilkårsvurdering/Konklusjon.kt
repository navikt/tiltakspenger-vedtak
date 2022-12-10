package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.leggSammen
import no.nav.tiltakspenger.domene.periode
import no.nav.tiltakspenger.domene.trekkFra

fun List<Vurdering>.konklusjonFor(vurderingsperiode: Periode): Konklusjon {

    val oppfylteVurderinger = this.filter { it.utfall == Utfall.OPPFYLT }
    val ikkeOppfyltVurderinger = this.filter { it.utfall == Utfall.IKKE_OPPFYLT }
    val manuelleVurderinger = this.filter { it.utfall == Utfall.KREVER_MANUELL_VURDERING }

    // For at vurderingsperioden skal være oppfylt i sin helhet, så må alle vilkår være oppfylt
    if (this.all { it.utfall == Utfall.OPPFYLT }) {
        return Konklusjon.Oppfylt(vurderingsperiode to this.map { it.vilkår }.toSet())
    }

    val ikkeOppfyltePerioderIVurderingsperioden = ikkeOppfyltVurderinger
        .map { it.periode() }
        .leggSammen()
        .filter { it.overlapperMed(vurderingsperiode) }
        .mapNotNull { it.overlappendePeriode(vurderingsperiode) }


    //Hvis dette slår til, så skal ikkeOppfyltePerioderIVurderingsperioden.size være 1:
    if (ikkeOppfyltePerioderIVurderingsperioden.any { it.inneholderHele(vurderingsperiode) }) {
        // Vi skal ikke kunne få ikke-oppfylte vilkårsvurderinger hvor den ikke-oppfylte perioden
        // er utenfor vurderingsperioden, da ville vilkåret vært oppfylt
        // Vi tar derfor med oss alle vilkårene her.
        // Merk at dette ikke tar høyde for at det kan være ulike vilkår som gjør at ulike deler av vurderingsperioden
        // ikke er oppfylt, så detaljerte er vi ikke. Ønsker man at for en gitt periode, så skal det være homogent
        // hvilke vilkår som har slått til, så må vi dele det opp mye mer.
        return Konklusjon.IkkeOppfylt(vurderingsperiode to ikkeOppfyltVurderinger.map { it.vilkår }.toSet())
    }

    val manuellePerioderIVurderingsperioden = manuelleVurderinger
        .map { it.periode() }
        .leggSammen()
        .trekkFra(ikkeOppfyltePerioderIVurderingsperioden)
        .filter { it.overlapperMed(vurderingsperiode) }
        .mapNotNull { it.overlappendePeriode(vurderingsperiode) }

    if (manuellePerioderIVurderingsperioden.isNotEmpty()) {
        // Selv om vi her deler det opp i flere perioder, så er det ikke nødvendigvis sånn at alle vilkårene
        // gjelder for hele perioden her heller
        val vilkårPerPeriode: Map<Periode, Set<Vilkår>> = manuellePerioderIVurderingsperioden
            .associateWith { manuellPeriode ->
                manuelleVurderinger
                    .filter { it.periode().overlapperMed(manuellPeriode) }
                    .map { it.vilkår }
                    .toSet()
            }
        return Konklusjon.KreverManuellBehandling(vilkårPerPeriode)
    }

    val oppfyltePerioderIVurderingsperioden: List<Periode> =
        vurderingsperiode.trekkFra(ikkeOppfyltePerioderIVurderingsperioden)

    //Alle vilkårene er nødvendigvis oppfylt i de periodene som er oppfylt
    val oppfylte: List<Konklusjon.Oppfylt> = oppfyltePerioderIVurderingsperioden
        .map { periode ->
            Konklusjon.Oppfylt(periode to oppfylteVurderinger.map { vurdering -> vurdering.vilkår }.toSet())
        }
    val ikkeOppfylte: List<Konklusjon.IkkeOppfylt> = ikkeOppfyltePerioderIVurderingsperioden
        .map { ikkeOppfyltPeriode ->
            Konklusjon.IkkeOppfylt(
                ikkeOppfyltPeriode to ikkeOppfyltVurderinger
                    .filter { it.periode().overlapperMed(ikkeOppfyltPeriode) }
                    .map { vurdering -> vurdering.vilkår }
                    .toSet()
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
