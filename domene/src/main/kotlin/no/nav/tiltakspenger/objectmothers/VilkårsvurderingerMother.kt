package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.vedtak.Institusjonsopphold
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.InstitusjonVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.InstitusjonsoppholdVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.KVPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.KommunaleYtelserVilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.LønnsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.LønnsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.PensjonsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.PensjonsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.StatligeYtelserVilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.VilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.Vilkårsvurderinger

private val defaultPeriode: Periode = Periode(1.januar(2022), 31.januar(2022))

fun nyPensjonsinntektVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
): PensjonsinntektVilkårsvurderingKategori {
    return PensjonsinntektVilkårsvurderingKategori(
        pensjonsinntektVilkårsvurdering = PensjonsinntektVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søknad = søknad,
        )
    )
}

fun nyLønnsinntektVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
): LønnsinntektVilkårsvurderingKategori {
    return LønnsinntektVilkårsvurderingKategori(
        lønnsinntektVilkårsvurdering = LønnsinntektVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søknad = søknad,
        )
    )
}

fun nyInstitusjonsVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
    institusjonsopphold: List<Institusjonsopphold> = emptyList(),
): InstitusjonVilkårsvurderingKategori {
    return InstitusjonVilkårsvurderingKategori(
        institusjonsoppholdVilkårsvurdering = InstitusjonsoppholdVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søknad = søknad,
            institusjonsopphold = institusjonsopphold,
        )
    )
}

fun nyAapVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    ytelser: List<YtelseSak> = emptyList(),
): AAPVilkårsvurdering {
    return AAPVilkårsvurdering(
        vurderingsperiode = vurderingsperiode,
        ytelser = ytelser,
    )
}

fun nyDagpengerVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    ytelser: List<YtelseSak> = emptyList(),
): DagpengerVilkårsvurdering {
    return DagpengerVilkårsvurdering(
        vurderingsperiode = vurderingsperiode,
        ytelser = ytelser,
    )
}

fun nyStatligeYtelserVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    aapVilkårsvurdering: AAPVilkårsvurdering = nyAapVilkårsvurdering(
        vurderingsperiode = vurderingsperiode
    ),
    dagpengerVilkårsvurdering: DagpengerVilkårsvurdering = nyDagpengerVilkårsvurdering(
        vurderingsperiode = vurderingsperiode
    ),
): StatligeYtelserVilkårsvurderinger {
    return StatligeYtelserVilkårsvurderinger(
        aap = aapVilkårsvurdering,
        dagpenger = dagpengerVilkårsvurdering,
    )
}

fun nyIntroprogrammetVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
): IntroProgrammetVilkårsvurdering {
    return IntroProgrammetVilkårsvurdering(
        søknad = søknad,
        vurderingsperiode = vurderingsperiode,
    )
}

fun nyKvpVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
): KVPVilkårsvurdering {
    return KVPVilkårsvurdering(
        søknad = søknad,
        vurderingsperiode = vurderingsperiode,
    )
}

fun nyKommunaleYtelserVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    introProgrammetVilkårsvurdering: IntroProgrammetVilkårsvurdering = nyIntroprogrammetVilkårsvurdering(
        vurderingsperiode = vurderingsperiode,
    ),
    kvpVilkårsvurdering: KVPVilkårsvurdering = nyKvpVilkårsvurdering(
        vurderingsperiode = vurderingsperiode,
    ),
): KommunaleYtelserVilkårsvurderinger {
    return KommunaleYtelserVilkårsvurderinger(
        intro = introProgrammetVilkårsvurdering,
        kvp = kvpVilkårsvurdering,
    )
}

fun nyVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    statligeYtelserVilkårsvurderinger: StatligeYtelserVilkårsvurderinger = nyStatligeYtelserVilkårsvurdering(
        vurderingsperiode = vurderingsperiode
    ),
    kommunaleYtelserVilkårsvurderinger: KommunaleYtelserVilkårsvurderinger = nyKommunaleYtelserVilkårsvurdering(
        vurderingsperiode = vurderingsperiode,
    ),
    pensjonsinntektVilkårsvurdering: VilkårsvurderingKategori = nyPensjonsinntektVilkårsvurdering(
        vurderingsperiode = vurderingsperiode,
    ),
    lønnsinntektVilkårsvurdering: LønnsinntektVilkårsvurderingKategori = nyLønnsinntektVilkårsvurdering(
        vurderingsperiode = vurderingsperiode,
    ),
    institusjonsoppholdVilkårsvurdering: InstitusjonVilkårsvurderingKategori = nyInstitusjonsVilkårsvurdering(
        vurderingsperiode = vurderingsperiode,
    ),
): Vilkårsvurderinger {
    return Vilkårsvurderinger(
        statligeYtelser = statligeYtelserVilkårsvurderinger,
        kommunaleYtelser = kommunaleYtelserVilkårsvurderinger,
        pensjonsordninger = pensjonsinntektVilkårsvurdering,
        lønnsinntekt = lønnsinntektVilkårsvurdering,
        institusjonopphold = institusjonsoppholdVilkårsvurdering,
    )
}
