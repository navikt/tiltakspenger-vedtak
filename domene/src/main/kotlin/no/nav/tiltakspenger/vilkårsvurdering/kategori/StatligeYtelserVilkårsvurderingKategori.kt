package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.IKKE_OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.KREVER_MANUELL_VURDERING
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AlderspensjonVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.ForeldrepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.GjenlevendepensjonVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OmsorgspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OpplæringspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OvergangsstønadVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerNærståendeVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerSyktBarnVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SupplerendeStønadAlderVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SupplerendeStønadFlyktningVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SvangerskapspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SykepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.UføreVilkarsvurdering

class StatligeYtelserVilkårsvurderingKategori(
    val aap: AAPVilkårsvurdering,
    val dagpenger: DagpengerVilkårsvurdering,
    val sykepenger: SykepengerVilkårsvurdering,
    val uføretrygd: UføreVilkarsvurdering,
    val overgangsstønad: OvergangsstønadVilkårsvurdering,
    val pleiepengerNærstående: PleiepengerNærståendeVilkårsvurdering,
    val pleiepengerSyktBarn: PleiepengerSyktBarnVilkårsvurdering,
    val foreldrepenger: ForeldrepengerVilkårsvurdering,
    val svangerskapspenger: SvangerskapspengerVilkårsvurdering,
    val gjenlevendepensjon: GjenlevendepensjonVilkårsvurdering,
    val supplerendeStønadFlyktning: SupplerendeStønadFlyktningVilkårsvurdering,
    val supplerendeStønadAlder: SupplerendeStønadAlderVilkårsvurdering,
    val alderspensjon: AlderspensjonVilkårsvurdering,
    val opplæringspenger: OpplæringspengerVilkårsvurdering,
    val omsorgspenger: OmsorgspengerVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.STATLIGEYTELSER

    override fun samletUtfall(): Utfall {
        val utfall = listOf(
            aap.samletUtfall(),
            dagpenger.samletUtfall(),
            sykepenger.samletUtfall(),
            uføretrygd.samletUtfall(),
            overgangsstønad.samletUtfall(),
            pleiepengerNærstående.samletUtfall(),
            pleiepengerSyktBarn.samletUtfall(),
            foreldrepenger.samletUtfall(),
            svangerskapspenger.samletUtfall(),
            gjenlevendepensjon.samletUtfall(),
            supplerendeStønadAlder.samletUtfall(),
            supplerendeStønadFlyktning.samletUtfall(),
            alderspensjon.samletUtfall(),
            opplæringspenger.samletUtfall(),
            omsorgspenger.samletUtfall(),
        )
        return when {
            utfall.any { it == IKKE_OPPFYLT } -> IKKE_OPPFYLT
            utfall.any { it == KREVER_MANUELL_VURDERING } -> KREVER_MANUELL_VURDERING
            else -> OPPFYLT
        }
    }

    override fun vurderinger(): List<Vurdering> =
        listOf(
            aap.vurderinger(),
            dagpenger.vurderinger(),
            sykepenger.vurderinger(),
            uføretrygd.vurderinger(),
            overgangsstønad.vurderinger(),
            pleiepengerNærstående.vurderinger(),
            pleiepengerSyktBarn.vurderinger(),
            foreldrepenger.vurderinger(),
            svangerskapspenger.vurderinger(),
            gjenlevendepensjon.vurderinger(),
            supplerendeStønadAlder.vurderinger(),
            supplerendeStønadFlyktning.vurderinger(),
            alderspensjon.vurderinger(),
            opplæringspenger.vurderinger(),
            omsorgspenger.vurderinger(),
        ).flatten()
}
