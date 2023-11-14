package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

class FraOgMedSpmVurdering(
    private val spm: Søknad.FraOgMedDatoSpm,
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
                fom = if (spm is Søknad.FraOgMedDatoSpm.Ja) {
                    spm.fra
                } else {
                    vurderingsperiode.fra
                },
                tom = vurderingsperiode.til,
                detaljer = detaljer(),
            )

            Utfall.KREVER_MANUELL_VURDERING -> Vurdering.KreverManuellVurdering(
                vilkår = vilkår,
                kilde = Kilde.SØKNAD,
                fom = if (spm is Søknad.FraOgMedDatoSpm.Ja) {
                    spm.fra
                } else {
                    vurderingsperiode.fra
                },
                tom = vurderingsperiode.til,
                detaljer = detaljer(),
            )
        }

    private fun detaljer(): String =
        when (spm) {
            is Søknad.FraOgMedDatoSpm.Ja -> "Svart JA i søknaden"
            is Søknad.FraOgMedDatoSpm.Nei -> "Svart NEI i søknaden"
        }

    fun avgjørUtfall() =
        when (spm) {
            is Søknad.FraOgMedDatoSpm.Ja -> Utfall.IKKE_OPPFYLT
            is Søknad.FraOgMedDatoSpm.Nei -> Utfall.OPPFYLT
        }
}
