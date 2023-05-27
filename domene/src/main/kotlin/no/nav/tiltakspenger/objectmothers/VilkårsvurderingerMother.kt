package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedTiltak
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.Institusjonsopphold
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.UføreVedtak
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
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AlderspensjonVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.EtterlønnVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.ForeldrepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.GjenlevendepensjonVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.InstitusjonsoppholdVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OmsorgspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OpplæringspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OvergangsstønadVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerNærståendeVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerSyktBarnVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PrivatPensjonsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SupplerendeStønadAlderVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SupplerendeStønadFlyktningVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SvangerskapspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SykepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.TiltakspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.UføreVilkarsvurdering
import java.time.LocalDate

private val defaultPeriode: Periode = Periode(1.januar(2022), 31.januar(2022))

interface VilkårsvurderingerMother {
    fun nyPensjonsinntektVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
    ): PensjonsinntektVilkårsvurderingKategori {
        return PensjonsinntektVilkårsvurderingKategori(
            privatPensjonsinntektVilkårsvurdering = PrivatPensjonsinntektVilkårsvurdering(
                vurderingsperiode = vurderingsperiode,
                søknad = søknad,
            ),
        )
    }

    fun nyLønnsinntektVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
    ): LønnsinntektVilkårsvurderingKategori {
        return LønnsinntektVilkårsvurderingKategori(
            etterlønnVilkårsvurdering = EtterlønnVilkårsvurdering(
                vurderingsperiode = vurderingsperiode,
                søknad = søknad,
            ),
        )
    }

    fun nyInstitusjonsVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
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

    fun nyUføreVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        uføreVedtak: UføreVedtak? = null,
    ): UføreVilkarsvurdering {
        return UføreVilkarsvurdering(
            uføreVedtak = uføreVedtak,
            vurderingsperiode = vurderingsperiode,
        )
    }

    fun nyOvergangsstønadVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        overgangsstønaderVedtak: List<OvergangsstønadVedtak> = emptyList(),
    ): OvergangsstønadVilkårsvurdering {
        return OvergangsstønadVilkårsvurdering(
            overgangsstønadVedtak = overgangsstønaderVedtak,
            vurderingsperiode = vurderingsperiode,
        )
    }

    fun nyPleiepengerNærståendeVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        ytelser: List<ForeldrepengerVedtak> = emptyList(),
    ): PleiepengerNærståendeVilkårsvurdering {
        return PleiepengerNærståendeVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            ytelser = ytelser,
        )
    }

    fun nyPleiepengerSyktBarnVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        ytelser: List<ForeldrepengerVedtak> = emptyList(),
    ): PleiepengerSyktBarnVilkårsvurdering {
        return PleiepengerSyktBarnVilkårsvurdering(
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

    fun nySupplerendeStønadAlderVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
    ): SupplerendeStønadAlderVilkårsvurdering {
        return SupplerendeStønadAlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søknad = søknad,
        )
    }

    fun nySupplerendeStønadFlyktningVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
    ): SupplerendeStønadFlyktningVilkårsvurdering {
        return SupplerendeStønadFlyktningVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søknad = søknad,
        )
    }

    fun nySykepengerVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
    ): SykepengerVilkårsvurdering {
        return SykepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søknad = søknad,
        )
    }

    fun nyGjenlevendepensjonVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
    ): GjenlevendepensjonVilkårsvurdering {
        return GjenlevendepensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søknad = søknad,
        )
    }

    fun nyAlderspensjonVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
    ): AlderspensjonVilkårsvurdering {
        return AlderspensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søknad = søknad,
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
        pleiepengerNærståendeVilkårsvurdering: PleiepengerNærståendeVilkårsvurdering = nyPleiepengerNærståendeVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        pleiepengerSyktBarnVilkårsvurdering: PleiepengerSyktBarnVilkårsvurdering = nyPleiepengerSyktBarnVilkårsvurdering(
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
        uføreVilkarsvurdering: UføreVilkarsvurdering = nyUføreVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        overgangsstønadVilkårsvurdering: OvergangsstønadVilkårsvurdering = nyOvergangsstønadVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        alderspensjonVilkårsvurdering: AlderspensjonVilkårsvurdering = nyAlderspensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        gjenlevendepensjonVilkårsvurdering: GjenlevendepensjonVilkårsvurdering = nyGjenlevendepensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        sykepengerVilkårsvurdering: SykepengerVilkårsvurdering = nySykepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        supplerendeStønadAlderVilkårsvurdering: SupplerendeStønadAlderVilkårsvurdering = nySupplerendeStønadAlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
        supplerendeStønadFlyktningVilkårsvurdering: SupplerendeStønadFlyktningVilkårsvurdering = nySupplerendeStønadFlyktningVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        ),
    ): StatligeYtelserVilkårsvurderingKategori {
        return StatligeYtelserVilkårsvurderingKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
            foreldrepenger = foreldrepengerVilkårsvurdering,
            pleiepengerNærstående = pleiepengerNærståendeVilkårsvurdering,
            pleiepengerSyktBarn = pleiepengerSyktBarnVilkårsvurdering,
            svangerskapspenger = svangerskapspengerVilkårsvurdering,
            opplæringspenger = opplæringspengerVilkårsvurdering,
            omsorgspenger = omsorgspengerVilkårsvurdering,
            uføretrygd = uføreVilkarsvurdering,
            overgangsstønad = overgangsstønadVilkårsvurdering,
            sykepenger = sykepengerVilkårsvurdering,
            alderspensjon = alderspensjonVilkårsvurdering,
            gjenlevendepensjon = gjenlevendepensjonVilkårsvurdering,
            supplerendeStønadFlyktning = supplerendeStønadFlyktningVilkårsvurdering,
            supplerendeStønadAlder = supplerendeStønadAlderVilkårsvurdering,
        )
    }

    fun nyIntroprogrammetVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
    ): IntroProgrammetVilkårsvurdering {
        return IntroProgrammetVilkårsvurdering(
            søknad = søknad,
            vurderingsperiode = vurderingsperiode,
        )
    }

    fun nyKvpVilkårsvurdering(
        vurderingsperiode: Periode = defaultPeriode,
        søknad: Søknad = nySøknadMedTiltak(),
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
