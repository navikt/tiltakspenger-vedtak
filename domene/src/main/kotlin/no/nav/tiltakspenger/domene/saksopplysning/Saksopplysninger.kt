package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.felles.Periode

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

    fun lagSaksopplysningerAvSøknad(søknad: Søknad): List<Saksopplysning> {
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
            // Ny saksopplysning er ikke fra saksbehandler
            if (this.first { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB } == saksopplysning) {
                // Vi har den nye saksopplysningen fra før

                // Ikke ta med saksopplysninger fra samme vilkår, utenom de fra saksbehandler.
                this.filterNot { it.vilkår == saksopplysning.vilkår && it.kilde != Kilde.SAKSB }
            } else {
                // Vi har ikke den nye saksopplysningen fra før

                // Ikke ta med saksopplysninger fra samme vilkår. Mao blir evt saksopplysninger fra Saksbehandler borte
                this.filterNot { it.vilkår == saksopplysning.vilkår }
            }
        } else {
            // Ny saksopplysning er fra saksbehandler

            // Ikke ta med saksopplysninger fra samme vilkår som er fra saksbehandler
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
    ): Saksopplysning {
        return Saksopplysning(
            fom = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.fra else periode.fra,
            tom = if (periodeSpm is Søknad.PeriodeSpm.Ja) periodeSpm.periode.til else periode.til,
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
            fom = periode.fra,
            tom = periode.til,
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
            fom = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) fraOgMedDatoSpm.fra else periode.fra,
            tom = periode.til,
            vilkår = vilkår,
            kilde = Kilde.SØKNAD,
            detaljer = "",
            typeSaksopplysning = if (fraOgMedDatoSpm is Søknad.FraOgMedDatoSpm.Ja) TypeSaksopplysning.HAR_YTELSE else TypeSaksopplysning.HAR_IKKE_YTELSE,
        )
    }
}
