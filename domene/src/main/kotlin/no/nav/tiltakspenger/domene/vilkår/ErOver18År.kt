package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfallsperiode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.fakta.FødselsdatoFakta

object ErOver18År : Vilkår<FødselsdatoFakta> {
    override val erInngangsVilkår: Boolean = true
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1

    override fun vurder(fakta: FødselsdatoFakta, vurderingsperiode: Periode): List<Utfallsperiode> {
        val faktum = fakta.system ?: return listOf(Utfallsperiode(Utfall.IkkeVurdert, vurderingsperiode))
        return when {
            vurderingsperiode.etter(faktum.fødselsdato) -> listOf(
                Utfallsperiode(
                    utfall = Utfall.VurdertOgOppfylt,
                    vurderingsperiode
                )
            )
            vurderingsperiode.før(faktum.fødselsdato) -> listOf(
                Utfallsperiode(
                    utfall = Utfall.VurdertOgIkkeOppfylt,
                    vurderingsperiode
                )
            )
            else -> listOf(
                Utfallsperiode(
                    utfall = Utfall.VurdertOgIkkeOppfylt,
                    Periode(vurderingsperiode.fra, faktum.fødselsdato.minusDays(1))
                ),
                Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, Periode(faktum.fødselsdato, vurderingsperiode.til))
            )
        }
    }
}
