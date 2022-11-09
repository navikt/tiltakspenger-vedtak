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
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SupplerendeStønadVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SvangerskapspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SykepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.UføretrygdVilkårsvurdering

@Suppress("LongParameterList")
class StatligeYtelserVilkårsvurderingKategori(
    val aap: AAPVilkårsvurdering,
    val dagpenger: DagpengerVilkårsvurdering,
    val sykepenger: SykepengerVilkårsvurdering =
        SykepengerVilkårsvurdering().leggTilIkkeImplementert(),
    val uføretrygd: UføretrygdVilkårsvurdering =
        UføretrygdVilkårsvurdering().leggTilIkkeImplementert(),
    val overgangsstønad: OvergangsstønadVilkårsvurdering =
        OvergangsstønadVilkårsvurdering().leggTilIkkeImplementert(),
    val pleiepenger: PleiepengerVilkårsvurdering =
        PleiepengerVilkårsvurdering().leggTilIkkeImplementert(),
    val foreldrepenger: ForeldrepengerVilkårsvurdering =
        ForeldrepengerVilkårsvurdering().leggTilIkkeImplementert(),
    val svangerskapspenger: SvangerskapspengerVilkårsvurdering =
        SvangerskapspengerVilkårsvurdering().leggTilIkkeImplementert(),
    val gjenlevendepensjon: GjenlevendepensjonVilkårsvurdering =
        GjenlevendepensjonVilkårsvurdering().leggTilIkkeImplementert(),
    val supplerendeStønad: SupplerendeStønadVilkårsvurdering =
        SupplerendeStønadVilkårsvurdering().leggTilIkkeImplementert(),
    val alderspensjon: AlderspensjonVilkårsvurdering =
        AlderspensjonVilkårsvurdering().leggTilIkkeImplementert(),
    val opplæringspenger: OpplæringspengerVilkårsvurdering =
        OpplæringspengerVilkårsvurdering().leggTilIkkeImplementert(),
    val omsorgspenger: OmsorgspengerVilkårsvurdering =
        OmsorgspengerVilkårsvurdering().leggTilIkkeImplementert(),
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.STATLIGEYTELSER

    override fun samletUtfall(): Utfall {
        val utfall = listOf(
            aap.samletUtfall(),
            dagpenger.samletUtfall(),
            sykepenger.samletUtfall(),
            uføretrygd.samletUtfall(),
            overgangsstønad.samletUtfall(),
            pleiepenger.samletUtfall(),
            foreldrepenger.samletUtfall(),
            svangerskapspenger.samletUtfall(),
            gjenlevendepensjon.samletUtfall(),
            supplerendeStønad.samletUtfall(),
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
            pleiepenger.vurderinger(),
            foreldrepenger.vurderinger(),
            svangerskapspenger.vurderinger(),
            gjenlevendepensjon.vurderinger(),
            supplerendeStønad.vurderinger(),
            alderspensjon.vurderinger(),
            opplæringspenger.vurderinger(),
            omsorgspenger.vurderinger(),
        ).flatten()
}
