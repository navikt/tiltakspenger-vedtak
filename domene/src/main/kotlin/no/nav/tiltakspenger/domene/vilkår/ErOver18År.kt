package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Utfallsperiode
import no.nav.tiltakspenger.domene.fakta.Faktum
import no.nav.tiltakspenger.domene.fakta.FødselsdatoFaktum
import kotlin.reflect.KClass

object ErOver18År : Vilkår {
    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(FødselsdatoFaktum::class)
    override val erInngangsVilkår: Boolean = true
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1

    override fun vurder(faktum: List<Faktum>, vurderingsperiode: Periode): List<Utfallsperiode> {
        faktum as List<FødselsdatoFaktum>
        return if (faktum.isEmpty()) {
            listOf(Utfallsperiode(utfall = Utfall.IkkeVurdert, vurderingsperiode))
        } else {
            vurder(faktum.first(), vurderingsperiode)
        }
    }

    private fun vurder(faktum: FødselsdatoFaktum, vurderingsperiode: Periode): List<Utfallsperiode> {
        return when {
            vurderingsperiode.etter(faktum.fødselsdato) -> listOf(
                Utfallsperiode(
                    utfall = Utfall.VurdertOgOppfylt,
                    vurderingsperiode,
                ),
            )

            vurderingsperiode.før(faktum.fødselsdato) -> listOf(
                Utfallsperiode(
                    utfall = Utfall.VurdertOgIkkeOppfylt,
                    vurderingsperiode,
                ),
            )

            else -> listOf(
                Utfallsperiode(
                    utfall = Utfall.VurdertOgIkkeOppfylt,
                    Periode(vurderingsperiode.fra, faktum.fødselsdato.minusDays(1)),
                ),
                Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, Periode(faktum.fødselsdato, vurderingsperiode.til)),
            )
        }
    }
}
