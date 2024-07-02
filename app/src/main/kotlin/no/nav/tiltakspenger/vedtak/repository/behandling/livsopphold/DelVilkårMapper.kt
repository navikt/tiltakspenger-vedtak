package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

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
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

internal object DelVilkårMapper {
    
    fun AlderspensjonDelVilkår.toDbJson(): DelVilkårMedSøknadDbJson {
        return DelVilkårMedSøknadDbJson(
            søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun GjenlevendepensjonDelVilkår.toDbJson(): DelVilkårMedSøknadDbJson {
        return DelVilkårMedSøknadDbJson(
            søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun SykepengerDelVilkår.toDbJson(): DelVilkårMedSøknadDbJson {
        return DelVilkårMedSøknadDbJson(
            søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun JobbsjansenDelVilkår.toDbJson(): DelVilkårMedSøknadDbJson {
        return DelVilkårMedSøknadDbJson(
            søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun PensjonsinntektDelVilkår.toDbJson(): DelVilkårMedSøknadDbJson {
        return DelVilkårMedSøknadDbJson(
            søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun SupplerendestønadFlyktningDelVilkår.toDbJson(): DelVilkårMedSøknadDbJson {
        return DelVilkårMedSøknadDbJson(
            søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun SupplerendestønadAlderDelVilkår.toDbJson(): DelVilkårMedSøknadDbJson {
        return DelVilkårMedSøknadDbJson(
            søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun AAPDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun DagpengerDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun ForeldrepengerDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun OmsorgspengerDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun OpplæringspengerDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun OvergangsstønadDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun PleiepengerNærståendeDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun PleiepengerSyktBarnDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun SvangerskapspengerDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }

    fun UføretrygdDelVilkår.toDbJson(): DelVilkårUtenSøknadDbJson {
        return DelVilkårUtenSøknadDbJson(
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
            utfallsperioder = utfall.toDbJson(),
        )
    }
}
