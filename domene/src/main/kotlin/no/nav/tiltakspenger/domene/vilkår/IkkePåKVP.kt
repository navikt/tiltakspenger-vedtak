import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.Faktum
import no.nav.tiltakspenger.domene.fakta.FaktumKilde
import no.nav.tiltakspenger.domene.fakta.KVPFaktum
import no.nav.tiltakspenger.domene.vilkår.Paragraf
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import kotlin.reflect.KClass

object IkkePåKVP : Vilkår {
    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(KVPFaktum::class)
    override val erInngangsVilkår: Boolean = true
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1

    override fun vurder(fakta: List<Faktum>, vurderingsperiode: Periode): List<Utfallsperiode> {
        val relevanteFakta = fakta
            .filter { it is KVPFaktum }
            .let { prioriterFakta(it) } as List<KVPFaktum>

        return vurder(relevanteFakta.first(), vurderingsperiode)
    }

    private fun vurder(faktum: KVPFaktum, vurderingsperiode: Periode): List<Utfallsperiode> {
        return when {
            faktum.deltarKVP && faktum.kilde == FaktumKilde.BRUKER ->
                listOf(Utfallsperiode(utfall= Utfall.VurdertOgTrengerManuellBehandling, periode = vurderingsperiode))
            !faktum.deltarKVP && faktum.kilde == FaktumKilde.SAKSBEHANDLER ->
                listOf(Utfallsperiode(utfall= Utfall.VurdertOgOppfylt,periode = vurderingsperiode))
            else -> listOf(Utfallsperiode(utfall= Utfall.VurdertOgIkkeOppfylt,periode = vurderingsperiode))
        }
    }
}
