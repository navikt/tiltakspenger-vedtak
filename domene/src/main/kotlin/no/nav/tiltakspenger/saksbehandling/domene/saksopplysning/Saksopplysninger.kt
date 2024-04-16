package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
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
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.TILTAKSPENGER),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.UFØRETRYGD),
            Saksopplysning.saksopplysningIkkeInnhentet(søknad.vurderingsperiode(), Vilkår.SVANGERSKAPSPENGER),
        )

    fun lagSaksopplysningerAvSøknad(søknad: Søknad): List<SaksopplysningInterface> {
        return listOf(
            lagSaksopplysningFraPeriodespørsmål(Vilkår.KVP, søknad.kvp, søknad.vurderingsperiode()),
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

    private fun lagSaksopplysningFraSøknad(søknad: Søknad): Saksopplysning {
        if (søknad.kvp is Søknad.PeriodeSpm.Ja) {
            return Saksopplysning(
                fom = søknad.kvp.periode.fra,
                tom = søknad.kvp.periode.til,
                vilkår = Vilkår.KVP,
                kilde = Kilde.SØKNAD,
                detaljer = "Har svart Ja i søknaden",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
            )
        } else {
            return Saksopplysning(
                fom = søknad.vurderingsperiode().fra,
                tom = søknad.vurderingsperiode().til,
                vilkår = Vilkår.KVP,
                kilde = Kilde.SØKNAD,
                detaljer = "Har svart Nei i søknaden",
                typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
            )
        }
    }

    private fun lagSaksopplysningFraPeriodespørsmål(
        vilkår: Vilkår,
        periodeSpm: Søknad.PeriodeSpm,
        periode: Periode,
    ): YtelseSaksopplysning {
        return YtelseSaksopplysning(
            periode = Periode(
                fra = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.fra else periode.fra,
                til = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.til else periode.til,
            ),
            vilkår = vilkår,
            kilde = Kilde.SØKNAD,
            detaljer = "",
            harYtelse = when (periodeSpm) {
                is Søknad.PeriodeSpm.Ja -> true
                is Søknad.PeriodeSpm.Nei -> false
            },
        )
    }

    private fun lagSaksopplysningFraJaNeiSpørsmål(
        vilkår: Vilkår,
        jaNeiSpm: Søknad.JaNeiSpm,
        periode: Periode,
    ): YtelseSaksopplysning {
        return YtelseSaksopplysning(
            periode = periode,
            vilkår = vilkår,
            kilde = Kilde.SØKNAD,
            detaljer = "",
            harYtelse = when (jaNeiSpm) {
                is Søknad.JaNeiSpm.Ja -> true
                is Søknad.JaNeiSpm.Nei -> false
            },
        )
    }

    private fun lagSaksopplysningFraFraOgMedDatospørsmål(
        vilkår: Vilkår,
        fraOgMedDatoSpm: Søknad.FraOgMedDatoSpm,
        periode: Periode,
    ): YtelseSaksopplysning {
        return YtelseSaksopplysning(
            periode = periode,
            vilkår = vilkår,
            kilde = Kilde.SØKNAD,
            detaljer = "",
            harYtelse = when (fraOgMedDatoSpm) {
                is Søknad.FraOgMedDatoSpm.Ja -> true
                is Søknad.FraOgMedDatoSpm.Nei -> false
            },
        )
    }
}
