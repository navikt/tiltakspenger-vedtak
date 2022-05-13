import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.KVPFaktum
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
            faktum.deltarKVP && faktum.kilde == FaktumKilde.BRUKER -> Utfall.VurdertOgTrengerManuellBehandling()
            !faktum.deltarKVP && faktum.kilde == FaktumKilde.SAKSBEHANDLER -> Utfall.VurdertOgOppfylt(vurderingsperiode)
            else -> Utfall.VurdertOgIkkeOppfylt()
        }
    }
}
