package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.AlderspensjonDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.GjenlevendepensjonDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.JobbsjansenDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PensjonsinntektDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SupplerendestønadAlderDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SupplerendestønadFlyktningDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SykepengerDelVilkår
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDomain

internal data class DelVilkårMedSøknadDbJson(
    val søknadSaksopplysning: LivsoppholdSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: LivsoppholdSaksopplysningDbJson?,
    val avklartSaksopplysning: LivsoppholdSaksopplysningDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toAlderspensjonDomain(vurderingsperiode: Periode): AlderspensjonDelVilkår {
        return AlderspensjonDelVilkår(
            vurderingsperiode = vurderingsperiode,
            utfall = utfallsperioder.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toAlderspensjonSaksbehandlerDomain(),
            søknadSaksopplysning = søknadSaksopplysning.toAlderspensjonSøknadDomain(),
            avklartSaksopplysning = if (avklartSaksopplysning.saksbehandler != null) avklartSaksopplysning.toAlderspensjonSaksbehandlerDomain() else avklartSaksopplysning.toAlderspensjonSøknadDomain(),
        )
    }

    fun toGjenlevendepensjonDomain(vurderingsperiode: Periode): GjenlevendepensjonDelVilkår {
        return GjenlevendepensjonDelVilkår(
            vurderingsperiode = vurderingsperiode,
            utfall = utfallsperioder.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toGjenlevendepensjonSaksbehandlerDomain(),
            søknadSaksopplysning = søknadSaksopplysning.toGjenlevendepensjonSøknadDomain(),
            avklartSaksopplysning = if (avklartSaksopplysning.saksbehandler != null) avklartSaksopplysning.toGjenlevendepensjonSaksbehandlerDomain() else avklartSaksopplysning.toGjenlevendepensjonSøknadDomain(),
        )
    }

    fun toSykepengerDomain(vurderingsperiode: Periode): SykepengerDelVilkår {
        return SykepengerDelVilkår(
            vurderingsperiode = vurderingsperiode,
            utfall = utfallsperioder.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toSykepengerSaksbehandlerDomain(),
            søknadSaksopplysning = søknadSaksopplysning.toSykepengerSøknadDomain(),
            avklartSaksopplysning = if (avklartSaksopplysning.saksbehandler != null) avklartSaksopplysning.toSykepengerSaksbehandlerDomain() else avklartSaksopplysning.toSykepengerSøknadDomain(),
        )
    }

    fun toJobbsjansenDomain(vurderingsperiode: Periode): JobbsjansenDelVilkår {
        return JobbsjansenDelVilkår(
            vurderingsperiode = vurderingsperiode,
            utfall = utfallsperioder.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toJobbsjansenSaksbehandlerDomain(),
            søknadSaksopplysning = søknadSaksopplysning.toJobbsjansenSøknadDomain(),
            avklartSaksopplysning = if (avklartSaksopplysning.saksbehandler != null) avklartSaksopplysning.toJobbsjansenSaksbehandlerDomain() else avklartSaksopplysning.toJobbsjansenSøknadDomain(),
        )
    }

    fun toPensjonsinntektDomain(vurderingsperiode: Periode): PensjonsinntektDelVilkår {
        return PensjonsinntektDelVilkår(
            vurderingsperiode = vurderingsperiode,
            utfall = utfallsperioder.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toPensjonsinntektSaksbehandlerDomain(),
            søknadSaksopplysning = søknadSaksopplysning.toPensjonsinntektSøknadDomain(),
            avklartSaksopplysning = if (avklartSaksopplysning.saksbehandler != null) avklartSaksopplysning.toPensjonsinntektSaksbehandlerDomain() else avklartSaksopplysning.toPensjonsinntektSøknadDomain(),
        )
    }

    fun toSupplerendeStønadAlderDomain(vurderingsperiode: Periode): SupplerendestønadAlderDelVilkår {
        return SupplerendestønadAlderDelVilkår(
            vurderingsperiode = vurderingsperiode,
            utfall = utfallsperioder.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toSupplerendestønadAlderSaksbehandlerDomain(),
            søknadSaksopplysning = søknadSaksopplysning.toSupplerendestønadAlderSøknadDomain(),
            avklartSaksopplysning = if (avklartSaksopplysning.saksbehandler != null) avklartSaksopplysning.toSupplerendestønadAlderSaksbehandlerDomain() else avklartSaksopplysning.toSupplerendestønadAlderSøknadDomain(),
        )
    }

    fun toSupplerendeStønadFlyktningDomain(vurderingsperiode: Periode): SupplerendestønadFlyktningDelVilkår {
        return SupplerendestønadFlyktningDelVilkår(
            vurderingsperiode = vurderingsperiode,
            utfall = utfallsperioder.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toSupplerendeStønadFlyktningSaksbehandlerDomain(),
            søknadSaksopplysning = søknadSaksopplysning.toSupplerendestønadFlyktningSøknadDomain(),
            avklartSaksopplysning = if (avklartSaksopplysning.saksbehandler != null) avklartSaksopplysning.toSupplerendeStønadFlyktningSaksbehandlerDomain() else avklartSaksopplysning.toSupplerendestønadFlyktningSøknadDomain(),
        )
    }
}
