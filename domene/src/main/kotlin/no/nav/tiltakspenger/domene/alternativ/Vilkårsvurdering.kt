package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Utfallsperiode
import kotlin.reflect.KClass

interface Vilkårsvurdering {
    fun vurder(periode: Periode): UtfallsperioderForVilkår
    fun fyllInnFaktumDerDetPasser(oppdatertFaktum: Faktum)

    fun finnIkkeVurderteVilkår(): List<Vilkår>
}

abstract class FaktaVilkårsvurdering<T>(private val clazz: KClass<T>) : Vilkårsvurdering where T : Faktum {
    private var faktum: T? = null

    override fun vurder(periode: Periode) = nullsafeVurder(faktum, periode)

    @Suppress("UNCHECKED_CAST")
    override fun fyllInnFaktumDerDetPasser(oppdatertFaktum: Faktum) {
        if (clazz.isInstance(oppdatertFaktum)) {
            faktum = oppdatertFaktum as T
        }
    }

    override fun finnIkkeVurderteVilkår() = if (faktum == null) listOf(vilkårForFaktum()) else emptyList()

    private fun nullsafeVurder(faktum: Faktum?, periode: Periode): UtfallsperioderForVilkår =
        faktum?.vurderFor(periode)
            ?: UtfallsperioderForVilkår(
                vilkårForFaktum(),
                listOf(Utfallsperiode(utfall = Utfall.IkkeVurdert, periode = periode))
            )

    private fun vilkårForFaktum(): Vilkår {
        println(clazz.qualifiedName)
        return when (clazz.qualifiedName) {
            BrukerOppgittKVPFaktum::class.qualifiedName -> BrukerOppgittKVPVilkår
            SaksbehandlerOppgittKVPFaktum::class.qualifiedName -> SaksbehandlerOppgittKVPVilkår
            FødselsdatoFaktum::class.qualifiedName -> Over18Vilkår
            else -> throw IllegalArgumentException("Should have used a sealed class")
        }
    }
}

class BrukerOppgittKVPVilkårsvurdering : FaktaVilkårsvurdering<BrukerOppgittKVPFaktum>(BrukerOppgittKVPFaktum::class)
class SaksbehandlerOppgittKVPVilkårsvurdering :
    FaktaVilkårsvurdering<SaksbehandlerOppgittKVPFaktum>(SaksbehandlerOppgittKVPFaktum::class)

class Over18Vilkårsvurdering : FaktaVilkårsvurdering<FødselsdatoFaktum>(FødselsdatoFaktum::class)

class KVPVilkårsvurdering(
    private val brukerOppgittKVPVilkårsvurdering: BrukerOppgittKVPVilkårsvurdering,
    private val saksbehandlerOppgittKVPVilkårsvurdering: SaksbehandlerOppgittKVPVilkårsvurdering
) : Vilkårsvurdering {
    override fun vurder(periode: Periode): UtfallsperioderForVilkår {
        return KVPVilkår.akkumuler(
            listOf(
                brukerOppgittKVPVilkårsvurdering.vurder(periode),
                saksbehandlerOppgittKVPVilkårsvurdering.vurder(periode)
            )
        )
    }

    override fun fyllInnFaktumDerDetPasser(faktum: Faktum) {
        brukerOppgittKVPVilkårsvurdering.fyllInnFaktumDerDetPasser(faktum)
        saksbehandlerOppgittKVPVilkårsvurdering.fyllInnFaktumDerDetPasser(faktum)
    }

    override fun finnIkkeVurderteVilkår(): List<Vilkår> =
        brukerOppgittKVPVilkårsvurdering.finnIkkeVurderteVilkår() + saksbehandlerOppgittKVPVilkårsvurdering.finnIkkeVurderteVilkår()
}