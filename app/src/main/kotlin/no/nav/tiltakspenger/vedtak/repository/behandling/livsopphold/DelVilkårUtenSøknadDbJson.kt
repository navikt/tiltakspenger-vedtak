package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.AAPDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.DagpengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.ForeldrepengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OmsorgspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OpplæringspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OvergangsstønadDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PleiepengerNærståendeDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PleiepengerSyktBarnDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SvangerskapspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.UføretrygdDelVilkår
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDomain

internal data class DelVilkårUtenSøknadDbJson(
    val saksbehandlerSaksopplysning: LivsoppholdSaksopplysningDbJson?,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toAapDomain(vurderingsperiode: Periode): AAPDelVilkår {
        return AAPDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toAapSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }

    fun toDagpengerDomain(vurderingsperiode: Periode): DagpengerDelVilkår {
        return DagpengerDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDagpengerSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }

    fun toForeldrePengerDomain(vurderingsperiode: Periode): ForeldrepengerDelVilkår {
        return ForeldrepengerDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toForeldrepengerSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }

    fun toOmsorgspengerDomain(vurderingsperiode: Periode): OmsorgspengerDelVilkår {
        return OmsorgspengerDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toOmsorgspengerSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }

    fun toOpplæringspengerDomain(vurderingsperiode: Periode): OpplæringspengerDelVilkår {
        return OpplæringspengerDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toOpplæringspengerSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }

    fun toOvergangsstønadDomain(vurderingsperiode: Periode): OvergangsstønadDelVilkår {
        return OvergangsstønadDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toOvergangsstønadSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }

    fun toPleiepengerNærståendeDomain(vurderingsperiode: Periode): PleiepengerNærståendeDelVilkår {
        return PleiepengerNærståendeDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toPleiepengerNærståendeSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }

    fun toPleiepengerSyktBarnDomain(vurderingsperiode: Periode): PleiepengerSyktBarnDelVilkår {
        return PleiepengerSyktBarnDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toPleiepengerSyktBarnSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }

    fun toSvangerskapspengerDomain(vurderingsperiode: Periode): SvangerskapspengerDelVilkår {
        return SvangerskapspengerDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toSvangerskapspengerSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }

    fun toUføretrygdDomain(vurderingsperiode: Periode): UføretrygdDelVilkår {
        return UføretrygdDelVilkår(
            vurderingsperiode = vurderingsperiode,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toUføretrygdSaksbehandlerDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }
}
