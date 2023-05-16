package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

class PeriodeSpmVurdering(
    private val spm: Søknad.PeriodeSpm,
    private val søknadVersjon: String,
    private val vilkår: Vilkår,
) {

    fun lagVurderingFraSøknad() =
        Vurdering(
            vilkår = vilkår,
            kilde = KommunalYtelseVilkårsvurdering.KILDE,
            fom = if (spm is Søknad.PeriodeSpm.Ja) {
                spm.periode.fra
            } else {
                null
            },
            tom = if (spm is Søknad.PeriodeSpm.Ja) {
                spm.periode.fra
            } else {
                null
            },
            utfall = avgjørUtfall(),
            detaljer = detaljer(),
        )

    private fun detaljer(): String =
        when (spm) {
            is Søknad.PeriodeSpm.IkkeMedISøknaden -> "Ikke med i søknaden"
            is Søknad.PeriodeSpm.IkkeRelevant -> "Vurdert som ikke relevant å spørre om"
            is Søknad.PeriodeSpm.Ja -> "Svart JA i søknaden"
            is Søknad.PeriodeSpm.Nei -> "Svart NEI i søknaden"
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
        }
}
