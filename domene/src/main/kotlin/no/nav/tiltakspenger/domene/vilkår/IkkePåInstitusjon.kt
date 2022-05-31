package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Utfallsperiode
import no.nav.tiltakspenger.domene.fakta.InstitusjonsoppholdsFakta

object IkkePåInstitusjon : Vilkår<InstitusjonsoppholdsFakta> {
    override val erInngangsVilkår: Boolean = true
    override val paragraf: Paragraf? = null

    override fun vurder(faktum: InstitusjonsoppholdsFakta, vurderingsperiode: Periode): List<Utfallsperiode> {

        fun regnUt(periode: Periode): List<Utfallsperiode> {
            val ikkeOppfylt = Utfallsperiode(utfall = Utfall.VurdertOgIkkeOppfylt, periode = periode)
            val oppfylt = vurderingsperiode.trekkFra(listOf(periode))
                .map { Utfallsperiode(utfall = Utfall.VurdertOgOppfylt, periode = it) }
            return listOf(ikkeOppfylt) + oppfylt
        }

        return when {
            faktum.saksbehandler != null -> regnUt(faktum.saksbehandler.oppholdsperiode)
            faktum.system != null -> regnUt(faktum.system.oppholdsperiode)
            faktum.bruker != null -> regnUt(faktum.bruker.oppholdsperiode)
            else -> return listOf(Utfallsperiode(utfall = Utfall.IkkeVurdert, periode = vurderingsperiode))
        }
    }
}
