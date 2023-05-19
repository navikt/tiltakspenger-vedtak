package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

class FraOgMedSpmVurdering(
    private val spm: Søknad.FraOgMedDatoSpm,
    private val vilkår: Vilkår,
) {

    companion object {
        const val KILDE = "Søknad"
    }

    fun lagVurderingFraSøknad() =
        Vurdering(
            vilkår = vilkår,
            kilde = KILDE,
            fom = if (spm is Søknad.FraOgMedDatoSpm.Ja) {
                spm.fra
            } else {
                null
            },
            tom = null,
            utfall = avgjørUtfall(),
            detaljer = detaljer(),
        )

    private fun detaljer(): String =
        when (spm) {
            is Søknad.FraOgMedDatoSpm.IkkeMedISøknaden -> "Ikke med i søknaden"
            is Søknad.FraOgMedDatoSpm.IkkeRelevant -> "Vurdert som ikke relevant å spørre om"
            is Søknad.FraOgMedDatoSpm.Ja -> "Svart JA i søknaden"
            is Søknad.FraOgMedDatoSpm.Nei -> "Svart NEI i søknaden"
            is Søknad.FraOgMedDatoSpm.FeilaktigBesvart -> "Feilaktig besvart i søknaden"
            is Søknad.FraOgMedDatoSpm.IkkeBesvart -> "Ikke besvart i søknaden"
        }

    fun avgjørUtfall() =
        when (spm) {
            is Søknad.FraOgMedDatoSpm.IkkeMedISøknaden -> Utfall.KREVER_MANUELL_VURDERING
            is Søknad.FraOgMedDatoSpm.IkkeRelevant -> Utfall.OPPFYLT
            is Søknad.FraOgMedDatoSpm.Ja -> Utfall.IKKE_OPPFYLT
            is Søknad.FraOgMedDatoSpm.Nei -> Utfall.OPPFYLT
            is Søknad.FraOgMedDatoSpm.FeilaktigBesvart -> Utfall.KREVER_MANUELL_VURDERING
            is Søknad.FraOgMedDatoSpm.IkkeBesvart -> Utfall.KREVER_MANUELL_VURDERING
        }
}
