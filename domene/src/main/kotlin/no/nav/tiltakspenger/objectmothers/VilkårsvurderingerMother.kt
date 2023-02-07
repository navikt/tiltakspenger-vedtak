package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.Institusjonsopphold
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Inngangsvilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.kategori.AlderVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.InstitusjonVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.KommunaleYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.LønnsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.PensjonsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.StatligeYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.TiltakspengerVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AlderVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.ForeldrepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.InstitusjonsoppholdVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.LønnsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OmsorgspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OpplæringspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PensjonsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SvangerskapspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.TiltakspengerVilkårsvurdering
import java.time.LocalDate

private val defaultPeriode: Periode = Periode(1.januar(2022), 31.januar(2022))

interface VilkårsvurderingerMother {
    fun nyPensjonsinntektVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedArenaTiltak(),
    ): PensjonsinntektVilkårsvurderingKategori {
        return PensjonsinntektVilkårsvurderingKategori(
            pensjonsinntektVilkårsvurdering = PensjonsinntektVilkårsvurdering(
                vurderingsperiode = vurderingsperiode,
                søknad = søknad,
            ),
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
            ),
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
                // institusjonsopphold = institusjonsopphold,
            ),
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

    fun nyForeldrepengerVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        ytelser: List<ForeldrepengerVedtak> = emptyList(),
    ): ForeldrepengerVilkårsvurdering {
        return ForeldrepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            ytelser = ytelser,
        )
    }

    fun nyPleiepengerVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        ytelser: List<ForeldrepengerVedtak> = emptyList(),
    ): PleiepengerVilkårsvurdering {
        return PleiepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            ytelser = ytelser,
        )
    }

    fun nyOmsorgspengerVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        ytelser: List<ForeldrepengerVedtak> = emptyList(),
    ): OmsorgspengerVilkårsvurdering {
        return OmsorgspengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            ytelser = ytelser,
        )
    }

    fun nyOpplæringspengerVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        ytelser: List<ForeldrepengerVedtak> = emptyList(),
    ): OpplæringspengerVilkårsvurdering {
        return OpplæringspengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            ytelser = ytelser,
        )
    }

    fun nySvangerskapspengerVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        ytelser: List<ForeldrepengerVedtak> = emptyList(),
    ): SvangerskapspengerVilkårsvurdering {
        return SvangerskapspengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            ytelser = ytelser,
        )
    }

    fun nyTiltakspengerVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        ytelser: List<YtelseSak> = emptyList(),
    ): TiltakspengerVilkårsvurderingKategori {
        return TiltakspengerVilkårsvurderingKategori(
            tiltakspengerVilkårsvurdering = TiltakspengerVilkårsvurdering(
                ytelser = ytelser,
                vurderingsperiode = vurderingsperiode,
            ),
        )
    }

    fun nyStatligeYtelserVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        aapVilkårsvurdering: AAPVilkårsvurdering = nyAapVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        dagpengerVilkårsvurdering: DagpengerVilkårsvurdering = nyDagpengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        foreldrepengerVilkårsvurdering: ForeldrepengerVilkårsvurdering = nyForeldrepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        pleiepengerVilkårsvurdering: PleiepengerVilkårsvurdering = nyPleiepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        svangerskapspengerVilkårsvurdering: SvangerskapspengerVilkårsvurdering = nySvangerskapspengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        opplæringspengerVilkårsvurdering: OpplæringspengerVilkårsvurdering = nyOpplæringspengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        omsorgspengerVilkårsvurdering: OmsorgspengerVilkårsvurdering = nyOmsorgspengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
    ): StatligeYtelserVilkårsvurderingKategori {
        return StatligeYtelserVilkårsvurderingKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
            foreldrepenger = foreldrepengerVilkårsvurdering,
            pleiepenger = pleiepengerVilkårsvurdering,
            svangerskapspenger = svangerskapspengerVilkårsvurdering,
            opplæringspenger = opplæringspengerVilkårsvurdering,
            omsorgspenger = omsorgspengerVilkårsvurdering,
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
    ): KommunaleYtelserVilkårsvurderingKategori {
        return KommunaleYtelserVilkårsvurderingKategori(
            intro = introProgrammetVilkårsvurdering,
            kvp = kvpVilkårsvurdering,
        )
    }

    fun nyAlderVilkårsvurdering(vurderingsperiode: Periode = defaultPeriode): AlderVilkårsvurderingKategori {
        return AlderVilkårsvurderingKategori(
            alderVilkårsvurdering = AlderVilkårsvurdering(
                vurderingsperiode = vurderingsperiode,
                søkersFødselsdato = LocalDate.now().minusYears(20),
            ),
        )
    }

    fun nyVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        statligeYtelserVilkårsvurderingKategori: StatligeYtelserVilkårsvurderingKategori = nyStatligeYtelserVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        kommunaleYtelserVilkårsvurderingKategori: KommunaleYtelserVilkårsvurderingKategori = nyKommunaleYtelserVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        pensjonsinntektVilkårsvurdering: PensjonsinntektVilkårsvurderingKategori = nyPensjonsinntektVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        lønnsinntektVilkårsvurdering: LønnsinntektVilkårsvurderingKategori = nyLønnsinntektVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        institusjonsoppholdVilkårsvurdering: InstitusjonVilkårsvurderingKategori = nyInstitusjonsVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        tiltakspengerVilkårsvurdering: TiltakspengerVilkårsvurderingKategori = nyTiltakspengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        alderVilkårsvurdering: AlderVilkårsvurderingKategori = nyAlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
    ): Inngangsvilkårsvurderinger {
        return Inngangsvilkårsvurderinger(
            tiltakspengerYtelser = tiltakspengerVilkårsvurdering,
            statligeYtelser = statligeYtelserVilkårsvurderingKategori,
            kommunaleYtelser = kommunaleYtelserVilkårsvurderingKategori,
            pensjonsordninger = pensjonsinntektVilkårsvurdering,
            lønnsinntekt = lønnsinntektVilkårsvurdering,
            institusjonopphold = institusjonsoppholdVilkårsvurdering,
            alder = alderVilkårsvurdering,
        )
    }
}
