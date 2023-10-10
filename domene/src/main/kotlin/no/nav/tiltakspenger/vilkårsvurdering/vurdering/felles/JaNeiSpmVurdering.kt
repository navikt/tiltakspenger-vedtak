package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

class JaNeiSpmVurdering(
    private val spm: Søknad.JaNeiSpm,
    private val vilkår: Vilkår,
    private val vurderingsperiode: Periode,
) {
    fun lagVurderingFraSøknad() =
        when (avgjørUtfall()) {
            Utfall.OPPFYLT -> Vurdering.Oppfylt(
                vilkår = vilkår,
                kilde = Kilde.SØKNAD,
                fom = vurderingsperiode.fra,
                tom = vurderingsperiode.til,
                detaljer = detaljer(),
            )

            Utfall.IKKE_OPPFYLT -> Vurdering.IkkeOppfylt(
                vilkår = vilkår,
                kilde = Kilde.SØKNAD,
                fom = vurderingsperiode.fra,
                tom = vurderingsperiode.til,
                detaljer = detaljer(),
            )

            Utfall.KREVER_MANUELL_VURDERING -> Vurdering.KreverManuellVurdering(
                vilkår = vilkår,
                kilde = Kilde.SØKNAD,
                fom = vurderingsperiode.fra,
                tom = vurderingsperiode.til,
                detaljer = detaljer(),
            )
        }

    private fun detaljer(): String =
        when (spm) {
            is Søknad.JaNeiSpm.IkkeMedISøknaden -> "Ikke med i søknaden"
            is Søknad.JaNeiSpm.IkkeRelevant -> "Vurdert som ikke relevant å spørre om"
            is Søknad.JaNeiSpm.Ja -> "Svart JA i søknaden"
            is Søknad.JaNeiSpm.Nei -> "Svart NEI i søknaden"
            is Søknad.JaNeiSpm.IkkeBesvart -> "Ikke besvart i søknaden"
        }

    fun avgjørUtfall() =
        when (spm) {
            is Søknad.JaNeiSpm.IkkeMedISøknaden -> Utfall.KREVER_MANUELL_VURDERING
            is Søknad.JaNeiSpm.IkkeRelevant -> Utfall.OPPFYLT
            is Søknad.JaNeiSpm.Ja -> Utfall.KREVER_MANUELL_VURDERING
            is Søknad.JaNeiSpm.Nei -> Utfall.OPPFYLT
            is Søknad.JaNeiSpm.IkkeBesvart -> Utfall.KREVER_MANUELL_VURDERING
        }
}
