package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

class PeriodeSpmVurdering(
    private val spm: Søknad.PeriodeSpm,
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
                fom = if (spm is Søknad.PeriodeSpm.Ja) {
                    spm.periode.fra
                } else {
                    vurderingsperiode.fra
                },
                tom = if (spm is Søknad.PeriodeSpm.Ja) {
                    spm.periode.til
                } else {
                    vurderingsperiode.til
                },
                detaljer = detaljer(),
            )

            Utfall.KREVER_MANUELL_VURDERING -> Vurdering.KreverManuellVurdering(
                vilkår = vilkår,
                kilde = Kilde.SØKNAD,
                fom = if (spm is Søknad.PeriodeSpm.Ja) {
                    spm.periode.fra
                } else {
                    vurderingsperiode.fra
                },
                tom = if (spm is Søknad.PeriodeSpm.Ja) {
                    spm.periode.til
                } else {
                    vurderingsperiode.til
                },
                detaljer = detaljer(),
            )
        }

    private fun detaljer(): String =
        when (spm) {
            is Søknad.PeriodeSpm.Ja -> "Svart JA i søknaden"
            is Søknad.PeriodeSpm.Nei -> "Svart NEI i søknaden"
        }

    fun avgjørUtfall() =
        when (spm) {
            is Søknad.PeriodeSpm.Ja -> Utfall.IKKE_OPPFYLT
            is Søknad.PeriodeSpm.Nei -> Utfall.OPPFYLT
        }
}
