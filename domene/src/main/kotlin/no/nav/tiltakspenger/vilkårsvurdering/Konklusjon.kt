package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.leggSammen
import no.nav.tiltakspenger.domene.trekkFra
import java.time.LocalDate

fun List<Vurdering>.konklusjonFor(vurderingsperiode: Periode): Konklusjon {
    fun Vurdering.periode() = Periode(this.fom ?: LocalDate.MIN, this.tom ?: LocalDate.MAX)

    val oppfylteVurderinger = this.filter { it.utfall == Utfall.OPPFYLT }
    val ikkeOppfyltVurderinger = this.filter { it.utfall == Utfall.IKKE_OPPFYLT }
    val manuelleVurderinger = this.filter { it.utfall == Utfall.KREVER_MANUELL_VURDERING }

    // For at vurderingsperioden skal være oppfylt i sin helhet, så må alle vilkår være oppfylt
    if (this.all { it.utfall == Utfall.OPPFYLT }) {
        return Konklusjon.Oppfylt(vurderingsperiode to this.toSet())
    }

    val ikkeOppfyltePerioderIVurderingsperioden = ikkeOppfyltVurderinger
        .map { it.periode() }
        .leggSammen()
        .filter { it.overlapperMed(vurderingsperiode) }
        .mapNotNull { it.overlappendePeriode(vurderingsperiode) }

    // Hvis dette slår til, så skal ikkeOppfyltePerioderIVurderingsperioden.size være 1:
    if (ikkeOppfyltePerioderIVurderingsperioden.any { it.inneholderHele(vurderingsperiode) }) {
        // Vi skal ikke kunne få ikke-oppfylte vilkårsvurderinger hvor den ikke-oppfylte perioden
        // er utenfor vurderingsperioden, da ville vilkåret vært oppfylt
        // Vi tar derfor med oss alle vilkårene her.
        // Merk at dette ikke tar høyde for at det kan være ulike vilkår som gjør at ulike deler av vurderingsperioden
        // ikke er oppfylt, så detaljerte er vi ikke. Ønsker man at for en gitt periode, så skal det være homogent
        // hvilke vilkår som har slått til, så må vi dele det opp mye mer.
        return Konklusjon.IkkeOppfylt(vurderingsperiode to ikkeOppfyltVurderinger.toSet())
    }

    val manuellePerioderIVurderingsperioden = manuelleVurderinger
        .map { it.periode() }
        .leggSammen()
        .trekkFra(ikkeOppfyltePerioderIVurderingsperioden)
        .filter { it.overlapperMed(vurderingsperiode) }
        .mapNotNull { it.overlappendePeriode(vurderingsperiode) }

    // Vi returnerer kun manuelle vurderinger hvis de finnes, selv om de ikke dekker hele vurderingsperioden.
    // Resonnementet er at de er det eneste som er relevant, man kan ikke fatte et vedtak for deler av perioden
    // så lenge det er noe som fremdeles må avklares
    if (manuellePerioderIVurderingsperioden.isNotEmpty()) {
        // Selv om vi her deler det opp i flere perioder, så er det ikke nødvendigvis sånn at alle vilkårene
        // gjelder for hele perioden her heller
        val vurderingPerPeriode: Map<Periode, Set<Vurdering>> = manuellePerioderIVurderingsperioden
            .associateWith { manuellPeriode ->
                manuelleVurderinger
                    .filter { it.periode().overlapperMed(manuellPeriode) }
                    .toSet()
            }
        return Konklusjon.KreverManuellBehandling(vurderingPerPeriode)
    }

    val oppfyltePerioderIVurderingsperioden: List<Periode> =
        vurderingsperiode.trekkFra(ikkeOppfyltePerioderIVurderingsperioden)

    val oppfylte: List<Konklusjon.Oppfylt> = oppfyltePerioderIVurderingsperioden
        .map { periode ->
            // Alle vilkårene er nødvendigvis oppfylt i de periodene som er oppfylt
            Konklusjon.Oppfylt(periode to oppfylteVurderinger.toSet())
        }
    val ikkeOppfylte: List<Konklusjon.IkkeOppfylt> = ikkeOppfyltePerioderIVurderingsperioden
        .map { ikkeOppfyltPeriode ->
            Konklusjon.IkkeOppfylt(
                ikkeOppfyltPeriode to ikkeOppfyltVurderinger
                    .filter { it.periode().overlapperMed(ikkeOppfyltPeriode) }
                    .toSet(),
            )
        }
    return Konklusjon.DelvisOppfylt(oppfylte, ikkeOppfylte)
}

sealed interface Konklusjon {

    data class Oppfylt(val periodeMedVilkår: Pair<Periode, Set<Vurdering>>) : Konklusjon

    data class IkkeOppfylt(val periodeMedVilkår: Pair<Periode, Set<Vurdering>>) : Konklusjon

    data class KreverManuellBehandling(val perioderMedVilkår: Map<Periode, Set<Vurdering>>) : Konklusjon

    data class DelvisOppfylt(val oppfylt: List<Oppfylt>, val ikkeOppfylt: List<IkkeOppfylt>) : Konklusjon
}
