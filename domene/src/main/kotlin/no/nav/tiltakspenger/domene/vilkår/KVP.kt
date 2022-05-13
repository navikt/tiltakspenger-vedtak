import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.KVPFaktum
import java.time.Period
import kotlin.reflect.KClass

object KVP : Vilkår {
    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(KVPFaktum::class)
    override val erInngangsVilkår: Boolean = true
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1

    override fun vurder(faktum: List<Faktum>, vurderingsperiode: Periode): Utfall {
        val kvpFaktum = faktum as List<KVPFaktum>
        return kvpFaktum.firstOrNull { it.kilde == FaktumKilde.SAKSBEHANDLER }?.let { vurder(it, vurderingsperiode) }
            ?: vurder(kvpFaktum.first(), vurderingsperiode)
    }

    private fun vurder(faktum: KVPFaktum, vurderingsperiode: Periode): Utfall {
        return when {
            faktum.deltarKVP && faktum.kilde == FaktumKilde.BRUKER -> Utfall.VURDERT_OG_TRENGER_MANUELL_VURDERING()
            !faktum.deltarKVP && faktum.kilde == FaktumKilde.SAKSBEHANDLER -> Utfall.VURDERT_OG_OPPFYLT(vurderingsperiode)
            else -> Utfall.VURDERT_OG_IKKE_OPPFYLT()
        }
    }
}
