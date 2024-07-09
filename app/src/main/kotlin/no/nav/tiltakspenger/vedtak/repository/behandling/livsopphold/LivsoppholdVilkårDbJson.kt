package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.AAPDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.AlderspensjonDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.DagpengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.ForeldrepengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.GjenlevendepensjonDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.JobbsjansenDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OmsorgspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OpplæringspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OvergangsstønadDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PensjonsinntektDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PleiepengerNærståendeDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PleiepengerSyktBarnDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SupplerendestønadAlderDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SupplerendestønadFlyktningDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SvangerskapspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SykepengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.UføretrygdDelVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.DelVilkårMapper.toDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDomain

internal data class LivsoppholdVilkårDbJson(
    val aapDelVilkår: DelVilkårUtenSøknadDbJson,
    val dagpengerDelVilkår: DelVilkårUtenSøknadDbJson,
    val foreldrepengerDelVilkår: DelVilkårUtenSøknadDbJson,
    val omsorgspengerDelVilkår: DelVilkårUtenSøknadDbJson,
    val opplæringspengerDelVilkår: DelVilkårUtenSøknadDbJson,
    val overgangsstønadDelVilkår: DelVilkårUtenSøknadDbJson,
    val pleiepengerNærståendeDelVilkår: DelVilkårUtenSøknadDbJson,
    val pleiepengerSyktBarnDelVilkår: DelVilkårUtenSøknadDbJson,
    val svangerskapspengerDelVilkår: DelVilkårUtenSøknadDbJson,
    val uføretrygdDelVilkår: DelVilkårUtenSøknadDbJson,
    val alderspensjonDelVilkår: DelVilkårMedSøknadDbJson,
    val gjenlevendePensjonDelVilkår: DelVilkårMedSøknadDbJson,
    val sykepengerDelVilkår: DelVilkårMedSøknadDbJson,
    val jobbsjansenDelVilkår: DelVilkårMedSøknadDbJson,
    val pensjonsinntektDelVilkår: DelVilkårMedSøknadDbJson,
    val supplerendestønadAlderDelVilkår: DelVilkårMedSøknadDbJson,
    val supplerendestønadFlyktningDelVilkår: DelVilkårMedSøknadDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(vurderingsperiode: Periode): LivsoppholdVilkår {
        return LivsoppholdVilkår.fromDb(
            vurderingsperiode = vurderingsperiode,
            delVilkår = setOf(
                aapDelVilkår.toAapDomain(vurderingsperiode),
                dagpengerDelVilkår.toDagpengerDomain(vurderingsperiode),
                foreldrepengerDelVilkår.toForeldrePengerDomain(vurderingsperiode),
                omsorgspengerDelVilkår.toOmsorgspengerDomain(vurderingsperiode),
                opplæringspengerDelVilkår.toOpplæringspengerDomain(vurderingsperiode),
                overgangsstønadDelVilkår.toOvergangsstønadDomain(vurderingsperiode),
                pleiepengerNærståendeDelVilkår.toPleiepengerNærståendeDomain(vurderingsperiode),
                pleiepengerSyktBarnDelVilkår.toPleiepengerSyktBarnDomain(vurderingsperiode),
                svangerskapspengerDelVilkår.toSvangerskapspengerDomain(vurderingsperiode),
                uføretrygdDelVilkår.toUføretrygdDomain(vurderingsperiode),
                alderspensjonDelVilkår.toAlderspensjonDomain(vurderingsperiode),
                gjenlevendePensjonDelVilkår.toGjenlevendepensjonDomain(vurderingsperiode),
                sykepengerDelVilkår.toSykepengerDomain(vurderingsperiode),
                jobbsjansenDelVilkår.toJobbsjansenDomain(vurderingsperiode),
                pensjonsinntektDelVilkår.toPensjonsinntektDomain(vurderingsperiode),
                supplerendestønadAlderDelVilkår.toSupplerendeStønadAlderDomain(vurderingsperiode),
                supplerendestønadFlyktningDelVilkår.toSupplerendeStønadFlyktningDomain(vurderingsperiode),
            ),
            utfall = utfallsperioder.toDomain(),
        )
    }
}

internal fun LivsoppholdVilkår.toDbJson(): LivsoppholdVilkårDbJson {
    return LivsoppholdVilkårDbJson(
        utfallsperioder = utfall.toDbJson(),
        aapDelVilkår = (this.delVilkår[LivsoppholdsytelseType.AAP]!! as AAPDelVilkår).toDbJson(),
        dagpengerDelVilkår = (this.delVilkår[LivsoppholdsytelseType.DAGPENGER]!! as DagpengerDelVilkår).toDbJson(),
        foreldrepengerDelVilkår = (this.delVilkår[LivsoppholdsytelseType.FORELDREPENGER]!! as ForeldrepengerDelVilkår).toDbJson(),
        omsorgspengerDelVilkår = (this.delVilkår[LivsoppholdsytelseType.OMSORGSPENGER]!! as OmsorgspengerDelVilkår).toDbJson(),
        opplæringspengerDelVilkår = (this.delVilkår[LivsoppholdsytelseType.OPPLÆRINGSPENGER]!! as OpplæringspengerDelVilkår).toDbJson(),
        overgangsstønadDelVilkår = (this.delVilkår[LivsoppholdsytelseType.OVERGANGSSTØNAD]!! as OvergangsstønadDelVilkår).toDbJson(),
        pleiepengerNærståendeDelVilkår = (this.delVilkår[LivsoppholdsytelseType.PLEIEPENGER_NÆRSTÅENDE]!! as PleiepengerNærståendeDelVilkår).toDbJson(),
        pleiepengerSyktBarnDelVilkår = (this.delVilkår[LivsoppholdsytelseType.PLEIEPENGER_SYKTBARN]!! as PleiepengerSyktBarnDelVilkår).toDbJson(),
        svangerskapspengerDelVilkår = (this.delVilkår[LivsoppholdsytelseType.SVANGERSKAPSPENGER]!! as SvangerskapspengerDelVilkår).toDbJson(),
        uføretrygdDelVilkår = (this.delVilkår[LivsoppholdsytelseType.UFØRETRYGD]!! as UføretrygdDelVilkår).toDbJson(),
        alderspensjonDelVilkår = (this.delVilkår[LivsoppholdsytelseType.ALDERSPENSJON]!! as AlderspensjonDelVilkår).toDbJson(),
        gjenlevendePensjonDelVilkår = (this.delVilkår[LivsoppholdsytelseType.GJENLEVENDEPENSJON]!! as GjenlevendepensjonDelVilkår).toDbJson(),
        sykepengerDelVilkår = (this.delVilkår[LivsoppholdsytelseType.SYKEPENGER]!! as SykepengerDelVilkår).toDbJson(),
        jobbsjansenDelVilkår = (this.delVilkår[LivsoppholdsytelseType.JOBBSJANSEN]!! as JobbsjansenDelVilkår).toDbJson(),
        pensjonsinntektDelVilkår = (this.delVilkår[LivsoppholdsytelseType.PENSJONSINNTEKT]!! as PensjonsinntektDelVilkår).toDbJson(),
        supplerendestønadAlderDelVilkår = (this.delVilkår[LivsoppholdsytelseType.SUPPLERENDESTØNAD_ALDER]!! as SupplerendestønadAlderDelVilkår).toDbJson(),
        supplerendestønadFlyktningDelVilkår = (this.delVilkår[LivsoppholdsytelseType.SUPPLERENDESTØNAD_FLYKTNING]!! as SupplerendestønadFlyktningDelVilkår).toDbJson(),
    )
}
