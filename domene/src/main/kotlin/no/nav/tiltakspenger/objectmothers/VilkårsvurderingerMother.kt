package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Inngangsvilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.kategori.InstitusjonVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.KommunaleYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.LønnsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.PensjonsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.StatligeYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.InstitusjonsoppholdVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.LønnsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PensjonsinntektVilkårsvurdering

private val defaultPeriode: Periode = Periode(1.januar(2022), 31.januar(2022))

fun nyPensjonsinntektVilkårsvurdering(
//    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
): PensjonsinntektVilkårsvurderingKategori {
    return PensjonsinntektVilkårsvurderingKategori(
        pensjonsinntektVilkårsvurdering = PensjonsinntektVilkårsvurdering().leggTilSøknad(søknad)
    )
}

fun nyLønnsinntektVilkårsvurdering(
//    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
): LønnsinntektVilkårsvurderingKategori {
    return LønnsinntektVilkårsvurderingKategori(
        lønnsinntektVilkårsvurdering = LønnsinntektVilkårsvurdering().leggTilSøknad(søknad)
    )
}

fun nyInstitusjonsVilkårsvurdering(
//    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
//    institusjonsopphold: List<Institusjonsopphold> = emptyList(),
): InstitusjonVilkårsvurderingKategori {
    return InstitusjonVilkårsvurderingKategori(
        institusjonsoppholdVilkårsvurdering = InstitusjonsoppholdVilkårsvurdering()
            .leggTilSøknad(søknad)
    )
}

fun nyAapVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    ytelser: List<YtelseSak> = emptyList(),
): AAPVilkårsvurdering {
    return AAPVilkårsvurdering().leggTilFakta(ytelser, vurderingsperiode)
//        vurderingsperiode = vurderingsperiode,
//        ytelser = ytelser,
//    )
}

fun nyDagpengerVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    ytelser: List<YtelseSak> = emptyList(),
): DagpengerVilkårsvurdering {
    return DagpengerVilkårsvurdering().leggTilFakta(ytelser, vurderingsperiode)
}

fun nyStatligeYtelserVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    aapVilkårsvurdering: AAPVilkårsvurdering = nyAapVilkårsvurdering(
        vurderingsperiode = vurderingsperiode
    ),
    dagpengerVilkårsvurdering: DagpengerVilkårsvurdering = nyDagpengerVilkårsvurdering(
        vurderingsperiode = vurderingsperiode
    ),
): StatligeYtelserVilkårsvurderingKategori {
    return StatligeYtelserVilkårsvurderingKategori(
        aap = aapVilkårsvurdering,
        dagpenger = dagpengerVilkårsvurdering,
    )
}

fun nyIntroprogrammetVilkårsvurdering(
//    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
): IntroProgrammetVilkårsvurdering {
    return IntroProgrammetVilkårsvurdering().leggTilSøknad(søknad)
}

fun nyKvpVilkårsvurdering(
//    vurderingsperiode: Periode = defaultPeriode,
    søknad: Søknad = nySøknadMedArenaTiltak(),
): KVPVilkårsvurdering {
    return KVPVilkårsvurdering().leggTilSøknad(søknad)
}

fun nyKommunaleYtelserVilkårsvurdering(
//    vurderingsperiode: Periode = defaultPeriode,
    introProgrammetVilkårsvurdering: IntroProgrammetVilkårsvurdering = nyIntroprogrammetVilkårsvurdering(
//        vurderingsperiode = vurderingsperiode,
    ),
    kvpVilkårsvurdering: KVPVilkårsvurdering = nyKvpVilkårsvurdering(
//        vurderingsperiode = vurderingsperiode,
    ),
): KommunaleYtelserVilkårsvurderingKategori {
    return KommunaleYtelserVilkårsvurderingKategori(
        intro = introProgrammetVilkårsvurdering,
        kvp = kvpVilkårsvurdering,
    )
}

@Suppress("LongParameterList")
fun nyVilkårsvurdering(
    vurderingsperiode: Periode = defaultPeriode,
    statligeYtelserVilkårsvurderingKategori: StatligeYtelserVilkårsvurderingKategori =
        nyStatligeYtelserVilkårsvurdering(vurderingsperiode = vurderingsperiode),
    kommunaleYtelserVilkårsvurderingKategori: KommunaleYtelserVilkårsvurderingKategori =
        nyKommunaleYtelserVilkårsvurdering(
//        vurderingsperiode = vurderingsperiode,
        ),
    pensjonsinntektVilkårsvurdering: PensjonsinntektVilkårsvurderingKategori = nyPensjonsinntektVilkårsvurdering(
//        vurderingsperiode = vurderingsperiode,
    ),
    lønnsinntektVilkårsvurdering: LønnsinntektVilkårsvurderingKategori = nyLønnsinntektVilkårsvurdering(
//        vurderingsperiode = vurderingsperiode,
    ),
    institusjonsoppholdVilkårsvurdering: InstitusjonVilkårsvurderingKategori = nyInstitusjonsVilkårsvurdering(
//        vurderingsperiode = vurderingsperiode,
    ),
): Inngangsvilkårsvurderinger {
    return Inngangsvilkårsvurderinger(
        statligeYtelser = statligeYtelserVilkårsvurderingKategori,
        kommunaleYtelser = kommunaleYtelserVilkårsvurderingKategori,
        pensjonsordninger = pensjonsinntektVilkårsvurdering,
        lønnsinntekt = lønnsinntektVilkårsvurdering,
        institusjonopphold = institusjonsoppholdVilkårsvurdering,
    )
}
