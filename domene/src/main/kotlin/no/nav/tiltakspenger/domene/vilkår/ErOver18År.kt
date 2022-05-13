package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.FødselsdatoFaktum
import java.time.LocalDate
import java.time.Period
import kotlin.reflect.KClass

object ErOver18År : Vilkår {
    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(FødselsdatoFaktum::class)
    val fødselsdato: LocalDate = LocalDate.now()
    override val erInngangsVilkår: Boolean = true
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1
    val vurderingsperiode: Periode
        get() = TODO("Not yet implemented")

    override fun vurder(faktum: List<Faktum>): Utfall {
        if (faktum.firstOrNull() !is FødselsdatoFaktum) return Utfall.IKKE_VURDERT
        return vurder((faktum.first() as FødselsdatoFaktum))
    }

    private fun vurder(faktum: FødselsdatoFaktum): Utfall {
        return when (faktum.erOver18()) {
            true -> Utfall.VURDERT_OG_OPPFYLT
            else -> Utfall.VURDERT_OG_IKKE_OPPFYLT
        }
    }
}
