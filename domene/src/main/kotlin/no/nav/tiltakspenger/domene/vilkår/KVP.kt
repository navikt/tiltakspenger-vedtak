import no.nav.tiltakspenger.domene.Paragraf
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Vilkår
import no.nav.tiltakspenger.domene.fakta.Faktum
import no.nav.tiltakspenger.domene.fakta.FaktumKilde
import no.nav.tiltakspenger.domene.fakta.KVPFaktum
import kotlin.reflect.KClass

object KVP : Vilkår {
    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(KVPFaktum::class)
    override val erInngangsVilkår: Boolean = true
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1

    override fun vurder(faktum: List<Faktum>, vurderingsperiode: Periode): List<Utfall> {
        val kvpFaktum = faktum as List<KVPFaktum>
        return kvpFaktum.firstOrNull { it.kilde == FaktumKilde.SAKSBEHANDLER }?.let { vurder(it, vurderingsperiode) }
            ?: vurder(kvpFaktum.first(), vurderingsperiode)
    }

    private fun vurder(faktum: KVPFaktum, vurderingsperiode: Periode): List<Utfall> {
        return when {
            faktum.deltarKVP && faktum.kilde == FaktumKilde.BRUKER ->
                listOf(Utfall.VurdertOgTrengerManuellBehandling(periode = vurderingsperiode))
            !faktum.deltarKVP && faktum.kilde == FaktumKilde.SAKSBEHANDLER ->
                listOf(Utfall.VurdertOgOppfylt(periode = vurderingsperiode))
            else -> listOf(Utfall.VurdertOgIkkeOppfylt(periode = vurderingsperiode))
        }
    }
}
