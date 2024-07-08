package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

object Saksopplysninger {
    fun initSaksopplysningerFraSøknad(søknad: Søknad): List<Saksopplysning> =
        listOf(
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.DAGPENGER),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.AAP),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.PLEIEPENGER_NÆRSTÅENDE),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.PLEIEPENGER_SYKT_BARN),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.FORELDREPENGER),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.OPPLÆRINGSPENGER),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.OMSORGSPENGER),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.ALDER),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.UFØRETRYGD),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.SVANGERSKAPSPENGER),
        )

    fun lagSaksopplysningerAvSøknad(søknad: Søknad): List<Saksopplysning> {
        return listOf(
            lagSaksopplysningFraPeriodespørsmål(Vilkår.INTROPROGRAMMET, søknad.intro, søknad.vurderingsperiode()),
            lagSaksopplysningFraPeriodespørsmål(
                Vilkår.INSTITUSJONSOPPHOLD,
                søknad.institusjon,
                søknad.vurderingsperiode(),
            ),
            lagSaksopplysningFraPeriodespørsmål(
                Vilkår.GJENLEVENDEPENSJON,
                søknad.gjenlevendepensjon,
                søknad.vurderingsperiode(),
            ),
            lagSaksopplysningFraPeriodespørsmål(Vilkår.SYKEPENGER, søknad.sykepenger, søknad.vurderingsperiode()),
            lagSaksopplysningFraPeriodespørsmål(
                Vilkår.SUPPLERENDESTØNADALDER,
                søknad.supplerendeStønadAlder,
                søknad.vurderingsperiode(),
            ),
            lagSaksopplysningFraPeriodespørsmål(
                Vilkår.SUPPLERENDESTØNADFLYKTNING,
                søknad.supplerendeStønadFlyktning,
                søknad.vurderingsperiode(),
            ),
            lagSaksopplysningFraPeriodespørsmål(Vilkår.JOBBSJANSEN, søknad.jobbsjansen, søknad.vurderingsperiode()),
            lagSaksopplysningFraPeriodespørsmål(
                Vilkår.PENSJONSINNTEKT,
                søknad.trygdOgPensjon,
                søknad.vurderingsperiode(),
            ),
            lagSaksopplysningFraFraOgMedDatospørsmål(
                Vilkår.ALDERSPENSJON,
                søknad.alderspensjon,
                søknad.vurderingsperiode(),
            ),
            lagSaksopplysningFraJaNeiSpørsmål(
                Vilkår.ETTERLØNN,
                søknad.etterlønn,
                søknad.vurderingsperiode(),
            ),
        )
    }

    fun List<Saksopplysning>.oppdaterSaksopplysninger(saksopplysning: Saksopplysning) =
        if (saksopplysning.kilde != Kilde.SAKSB) {
            if (this.first { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB } == saksopplysning) {
                this.filterNot { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB }
            } else {
                this.filterNot { it.vilkår == saksopplysning.vilkår }
            }
        } else {
            this.filterNot { it.vilkår == saksopplysning.vilkår && it.kilde == Kilde.SAKSB }
        }.plus(saksopplysning)

    private fun lagSaksopplysningFraPeriodespørsmål(
        vilkår: Vilkår,
        periodeSpm: Søknad.PeriodeSpm,
        periode: Periode,
    ): Saksopplysning {
        return Saksopplysning(
            fom = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.fraOgMed else periode.fraOgMed,
            tom = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.tilOgMed else periode.tilOgMed,
            vilkår = vilkår,
            kilde = Kilde.SØKNAD,
            detaljer = "",
            typeSaksopplysning = if (periodeSpm is Søknad.PeriodeSpm.Ja) TypeSaksopplysning.HAR_YTELSE else TypeSaksopplysning.HAR_IKKE_YTELSE,
        )
    }

    private fun lagSaksopplysningFraJaNeiSpørsmål(
        vilkår: Vilkår,
        jaNeiSpm: Søknad.JaNeiSpm,
        periode: Periode,
    ): Saksopplysning {
        return Saksopplysning(
            fom = periode.fraOgMed,
            tom = periode.tilOgMed,
            vilkår = vilkår,
            kilde = Kilde.SØKNAD,
            detaljer = "",
            typeSaksopplysning = if (jaNeiSpm is Søknad.JaNeiSpm.Ja) TypeSaksopplysning.HAR_YTELSE else TypeSaksopplysning.HAR_IKKE_YTELSE,
        )
    }

    private fun lagSaksopplysningFraFraOgMedDatospørsmål(
        vilkår: Vilkår,
        fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm,
        periode: Periode,
    ): Saksopplysning {
        return Saksopplysning(
            fom = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) fraOgMedDatoSpm.fra else periode.fraOgMed,
            tom = periode.tilOgMed,
            vilkår = vilkår,
            kilde = Kilde.SØKNAD,
            detaljer = "",
            typeSaksopplysning = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) TypeSaksopplysning.HAR_YTELSE else TypeSaksopplysning.HAR_IKKE_YTELSE,
        )
    }
}
