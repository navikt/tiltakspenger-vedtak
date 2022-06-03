import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.*
import no.nav.tiltakspenger.domene.vilkår.Paragraf
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import kotlin.reflect.KClass

object IkkePåKVP : Vilkår<KVPFakta> {
    override val erInngangsVilkår: Boolean = true
    override val paragraf = Paragraf.PARAGRAF_3_LEDD_1_PUNKTUM1

    override fun vurder(faktum: KVPFakta, vurderingsperiode: Periode): List<Utfallsperiode> {
        return when {
            faktum.saksbehandler != null -> {
                val utfall = if (faktum.saksbehandler.deltarKVP) Utfall.VurdertOgIkkeOppfylt else Utfall.VurdertOgOppfylt
                listOf(Utfallsperiode(utfall, vurderingsperiode))
            }

            faktum.bruker?.deltarKVP == true ->
                listOf(Utfallsperiode(utfall = Utfall.VurdertOgTrengerManuellBehandling, periode = vurderingsperiode))
            else -> listOf(Utfallsperiode(utfall = Utfall.VurdertOgIkkeOppfylt,periode = vurderingsperiode))
        }
    }
}
