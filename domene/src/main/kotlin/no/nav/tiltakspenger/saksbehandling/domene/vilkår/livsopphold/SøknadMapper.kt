package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad

object SøknadMapper {

    fun mapSøknadTilLivsoppholdSaksopplysninger(søknad: Søknad): LivsoppholdSaksopplysningerFraSøknad {

        val alderspensjonSøknadSaksopplysning: AlderspensjonSaksopplysning.Søknad =
            AlderspensjonSaksopplysning.Søknad(
                periodiseringAvFraOgMedDatoSpm(
                    søknad.vurderingsperiode(),
                    søknad.alderspensjon,
                ),
                søknad.tidsstempelHosOss,
            )
        val gjenlevendeSøknadSaksopplysning: GjenlevendepensjonSaksopplysning.Søknad =
            GjenlevendepensjonSaksopplysning.Søknad(
                periodiseringAvPeriodeSpm(
                    søknad.vurderingsperiode(),
                    søknad.gjenlevendepensjon,
                ),
                søknad.tidsstempelHosOss,
            )
        val sykepengerSøknadSaksopplysning: SykepengerSaksopplysning.Søknad =
            SykepengerSaksopplysning.Søknad(
                periodiseringAvPeriodeSpm(søknad.vurderingsperiode(), søknad.sykepenger),
                søknad.tidsstempelHosOss,
            )
        val jobbsjansenSøknadSaksopplysning: JobbsjansenSaksopplysning.Søknad =
            JobbsjansenSaksopplysning.Søknad(
                periodiseringAvPeriodeSpm(søknad.vurderingsperiode(), søknad.jobbsjansen),
                søknad.tidsstempelHosOss,
            )
        val pensjonsinntektSøknadSaksopplysning: PensjonsinntektSaksopplysning.Søknad =
            PensjonsinntektSaksopplysning.Søknad(
                periodiseringAvPeriodeSpm(
                    søknad.vurderingsperiode(),
                    søknad.trygdOgPensjon,
                ),
                søknad.tidsstempelHosOss,
            )
        val supplerendestønadAlderSøknadSaksopplysning: SupplerendeStønadAlderSaksopplysning.Søknad =
            SupplerendeStønadAlderSaksopplysning.Søknad(
                periodiseringAvPeriodeSpm(
                    søknad.vurderingsperiode(),
                    søknad.supplerendeStønadAlder,
                ),
                søknad.tidsstempelHosOss,
            )
        val supplerendestønadFlyktningSøknadSaksopplysning: SupplerendeStønadFlyktningSaksopplysning.Søknad =
            SupplerendeStønadFlyktningSaksopplysning.Søknad(
                periodiseringAvPeriodeSpm(
                    søknad.vurderingsperiode(),
                    søknad.supplerendeStønadFlyktning,
                ),
                søknad.tidsstempelHosOss,
            )
        return LivsoppholdSaksopplysningerFraSøknad(
            alderspensjonSøknadSaksopplysning,
            gjenlevendeSøknadSaksopplysning,
            sykepengerSøknadSaksopplysning,
            jobbsjansenSøknadSaksopplysning,
            pensjonsinntektSøknadSaksopplysning,
            supplerendestønadAlderSøknadSaksopplysning,
            supplerendestønadFlyktningSøknadSaksopplysning,
            supplerendestønadFlyktningSøknadSaksopplysning,
        )
    }

    private fun periodiseringAvPeriodeSpm(
        søknadsperiode: Periode,
        periodeSpm: Søknad.PeriodeSpm,
    ): Periodisering<HarYtelse> {
        return if (periodeSpm is Søknad.PeriodeSpm.Ja) {
            Periodisering(
                HarYtelse.HAR_IKKE_YTELSE,
                søknadsperiode,
            ).setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, periodeSpm.periode)
        } else {
            Periodisering(HarYtelse.HAR_IKKE_YTELSE, søknadsperiode)
        }
    }

    private fun periodiseringAvFraOgMedDatoSpm(
        søknadsperiode: Periode,
        fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm,
    ): Periodisering<HarYtelse> {
        return if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) {
            Periodisering(HarYtelse.HAR_IKKE_YTELSE, søknadsperiode)
                .setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, Periode(fraOgMedDatoSpm.fra, søknadsperiode.tilOgMed))
        } else {
            Periodisering(HarYtelse.HAR_IKKE_YTELSE, søknadsperiode)
        }
    }

    private fun periodiseringAvJaNeiSpm(søknadsperiode: Periode, jaNeiSpm: Søknad.JaNeiSpm): Periodisering<HarYtelse> {
        return Periodisering(
            if (jaNeiSpm is Søknad.JaNeiSpm.Ja) HarYtelse.HAR_YTELSE else HarYtelse.HAR_IKKE_YTELSE,
            søknadsperiode,
        )
    }

}
