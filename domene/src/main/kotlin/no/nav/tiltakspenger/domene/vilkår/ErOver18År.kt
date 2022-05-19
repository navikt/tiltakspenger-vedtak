package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.Faktum
import no.nav.tiltakspenger.domene.fakta.FødselsdatoFaktum
import kotlin.reflect.KClass

object ErOver18År : Vilkår {
    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(FødselsdatoFaktum::class)
    override val erInngangsVilkår: Boolean = true
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1

    override fun vurder(faktum: List<Faktum>, vurderingsperiode: Periode): List<Utfallsperiode> {
        if (faktum.firstOrNull() !is FødselsdatoFaktum) return listOf(Utfallsperiode(utfall= Utfall.IkkeVurdert, vurderingsperiode))
        return vurder((faktum.first() as FødselsdatoFaktum), vurderingsperiode)
    }

    private fun vurder(faktum: FødselsdatoFaktum, vurderingsperiode: Periode): List<Utfallsperiode> {
        return when {
            vurderingsperiode.etter(faktum.fødselsdato) -> listOf(Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, vurderingsperiode))
            vurderingsperiode.før(faktum.fødselsdato) -> listOf(Utfallsperiode(utfall = Utfall.VurdertOgIkkeOppfylt,vurderingsperiode))
            else -> listOf(
                Utfallsperiode(utfall=Utfall.VurdertOgIkkeOppfylt, Periode(vurderingsperiode.fra, faktum.fødselsdato.minusDays(1))),
                Utfallsperiode(utfall=Utfall.VurdertOgOppfylt,Periode(faktum.fødselsdato, vurderingsperiode.til))
            )
        }
    }
}
