package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

private val LOG = KotlinLogging.logger {}

class UklartPeriodeSpmVurdering(
    private val spm: Søknad.PeriodeSpm,
    private val søknadVersjon: String,
    private val vilkår: Vilkår,
    private val vurderingsperiode: Periode,
) {

    companion object {
        const val KILDE = "Søknad"
    }

    fun lagVurderingFraSøknad() =
        Vurdering(
            vilkår = vilkår,
            kilde = KILDE,
            fom = when (spm) {
                is Søknad.PeriodeSpm.IkkeMedISøknaden -> vurderingsperiode.fra
                is Søknad.PeriodeSpm.IkkeRelevant -> null
                is Søknad.PeriodeSpm.Ja -> spm.periode.fra
                is Søknad.PeriodeSpm.Nei -> null
                is Søknad.PeriodeSpm.FeilaktigBesvart -> vurderingsperiode.fra
                is Søknad.PeriodeSpm.IkkeBesvart -> vurderingsperiode.fra
            },
            tom = when (spm) {
                is Søknad.PeriodeSpm.IkkeMedISøknaden -> vurderingsperiode.til
                is Søknad.PeriodeSpm.IkkeRelevant -> null
                is Søknad.PeriodeSpm.Ja -> spm.periode.til
                is Søknad.PeriodeSpm.Nei -> null
                is Søknad.PeriodeSpm.FeilaktigBesvart -> vurderingsperiode.til
                is Søknad.PeriodeSpm.IkkeBesvart -> vurderingsperiode.til
            },
            utfall = avgjørUtfall(),
            detaljer = detaljer(),
        ).also {
            LOG.info { "Fom er ${it.fom} for intro, søknadsversjon er $søknadVersjon" }
        }

    private fun detaljer(): String =
        when (spm) {
            is Søknad.PeriodeSpm.IkkeMedISøknaden -> "Ikke med i søknaden"
            is Søknad.PeriodeSpm.IkkeRelevant -> "Vurdert som ikke relevant å spørre om"
            is Søknad.PeriodeSpm.Ja -> "Svart JA i søknaden"
            is Søknad.PeriodeSpm.Nei -> "Svart NEI i søknaden"
            is Søknad.PeriodeSpm.FeilaktigBesvart -> "Feilaktig besvart i søknaden"
            is Søknad.PeriodeSpm.IkkeBesvart -> "Ikke besvart i søknaden"
        }

    fun avgjørUtfall() =
        when (spm) {
            is Søknad.PeriodeSpm.IkkeMedISøknaden -> Utfall.KREVER_MANUELL_VURDERING
            is Søknad.PeriodeSpm.IkkeRelevant -> Utfall.OPPFYLT
            is Søknad.PeriodeSpm.Ja -> if (søknadVersjon == "1") {
                Utfall.KREVER_MANUELL_VURDERING
            } else {
                Utfall.IKKE_OPPFYLT
            }

            is Søknad.PeriodeSpm.Nei -> Utfall.OPPFYLT
            is Søknad.PeriodeSpm.FeilaktigBesvart -> Utfall.KREVER_MANUELL_VURDERING
            is Søknad.PeriodeSpm.IkkeBesvart -> Utfall.KREVER_MANUELL_VURDERING
        }.also {
            LOG.info { "Utfallet er $it for intro, søknadsversjon er $søknadVersjon" }
        }
}
