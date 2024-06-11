package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår

object SøknadTilSaksopplysningMapper {

    fun lagYtelseSaksopplysningFraPeriodeSpørsmål(
        vilkår: Inngangsvilkår,
        periodeSpm: Søknad.PeriodeSpm,
        søknadsperiode: Periode,
    ): YtelseSaksopplysning {
        return YtelseSaksopplysning(
            kilde = Kilde.SØKNAD,
            vilkår = vilkår,
            detaljer = "",
            harYtelse = if (periodeSpm is Søknad.PeriodeSpm.Ja) {
                Periodisering(
                    HarYtelse.HAR_IKKE_YTELSE,
                    søknadsperiode,
                ).setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, periodeSpm.periode)
            } else {
                Periodisering(HarYtelse.HAR_IKKE_YTELSE, søknadsperiode)
            },
            saksbehandler = null,
        )
    }

    fun lagSaksopplysningFraFraOgMedDatospørsmål(
        vilkår: Inngangsvilkår,
        fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm,
        søknadsperiode: Periode,
    ): YtelseSaksopplysning {
        return YtelseSaksopplysning(
            kilde = Kilde.SØKNAD,
            vilkår = vilkår,
            detaljer = "",
            harYtelse = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) {
                Periodisering(HarYtelse.HAR_IKKE_YTELSE, søknadsperiode)
                    .setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, Periode(fraOgMedDatoSpm.fra, søknadsperiode.til))
            } else {
                Periodisering(HarYtelse.HAR_IKKE_YTELSE, søknadsperiode)
            },
            saksbehandler = null,
        )
    }

    fun lagSaksopplysningFraJaNeiSpørsmål(
        vilkår: Inngangsvilkår,
        jaNeiSpm: Søknad.JaNeiSpm,
        søknadsperiode: Periode,
    ): YtelseSaksopplysning {
        return YtelseSaksopplysning(
            kilde = Kilde.SØKNAD,
            vilkår = vilkår,
            detaljer = "",
            harYtelse = Periodisering(
                if (jaNeiSpm is Søknad.JaNeiSpm.Ja) HarYtelse.HAR_YTELSE else HarYtelse.HAR_IKKE_YTELSE,
                søknadsperiode,
            ),
            saksbehandler = null,
        )
    }

    fun lagYtelseSaksopplysningFraPeriodeSpørsmål(
        vilkår: LivsoppholdDelVilkår,
        periodeSpm: Søknad.PeriodeSpm,
        søknadsperiode: Periode,
    ): LivsoppholdYtelseSaksopplysning {
        return LivsoppholdYtelseSaksopplysning(
            kilde = Kilde.SØKNAD,
            vilkår = vilkår,
            detaljer = "",
            harYtelse = if (periodeSpm is Søknad.PeriodeSpm.Ja) {
                Periodisering(
                    HarYtelse.HAR_IKKE_YTELSE,
                    søknadsperiode,
                ).setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, periodeSpm.periode)
            } else {
                Periodisering(HarYtelse.HAR_IKKE_YTELSE, søknadsperiode)
            },
            saksbehandler = null,
        )
    }

    fun lagSaksopplysningFraFraOgMedDatospørsmål(
        vilkår: LivsoppholdDelVilkår,
        fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm,
        søknadsperiode: Periode,
    ): LivsoppholdYtelseSaksopplysning {
        return LivsoppholdYtelseSaksopplysning(
            kilde = Kilde.SØKNAD,
            vilkår = vilkår,
            detaljer = "",
            harYtelse = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) {
                Periodisering(HarYtelse.HAR_IKKE_YTELSE, søknadsperiode)
                    .setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, Periode(fraOgMedDatoSpm.fra, søknadsperiode.til))
            } else {
                Periodisering(HarYtelse.HAR_IKKE_YTELSE, søknadsperiode)
            },
            saksbehandler = null,
        )
    }

    fun lagSaksopplysningFraJaNeiSpørsmål(
        vilkår: LivsoppholdDelVilkår,
        jaNeiSpm: Søknad.JaNeiSpm,
        søknadsperiode: Periode,
    ): LivsoppholdYtelseSaksopplysning {
        return LivsoppholdYtelseSaksopplysning(
            kilde = Kilde.SØKNAD,
            vilkår = vilkår,
            detaljer = "",
            harYtelse = Periodisering(
                if (jaNeiSpm is Søknad.JaNeiSpm.Ja) HarYtelse.HAR_YTELSE else HarYtelse.HAR_IKKE_YTELSE,
                søknadsperiode,
            ),
            saksbehandler = null,
        )
    }
}
