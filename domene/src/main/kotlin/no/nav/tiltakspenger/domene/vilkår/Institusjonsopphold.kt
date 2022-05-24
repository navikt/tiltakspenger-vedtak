package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.Faktum
import no.nav.tiltakspenger.domene.fakta.InstitusjonsoppholdsFaktum
import kotlin.reflect.KClass

object Institusjonsopphold : Vilkår {
    override val erInngangsVilkår: Boolean = true
    override val paragraf: Paragraf? = null
    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(InstitusjonsoppholdsFaktum::class)

    override fun vurder(faktum: List<Faktum>, vurderingsperiode: Periode): List<Utfallsperiode> {
        val instFaktum = (faktum as List<InstitusjonsoppholdsFaktum>).first()

        return if (!instFaktum.opphold) {
            listOf(Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, periode = vurderingsperiode))
        } else {
            val ikkeOppfylt =
                instFaktum.oppholdsperiode.map { Utfallsperiode(utfall = Utfall.VurdertOgIkkeOppfylt, periode = it) }
            val oppfylt = vurderingsperiode.trekkFra(instFaktum.oppholdsperiode)
                .map { Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, periode = it) }
            //TODO: Sorter i korrekt rekkefølge
            ikkeOppfylt + oppfylt
        }
    }
}
