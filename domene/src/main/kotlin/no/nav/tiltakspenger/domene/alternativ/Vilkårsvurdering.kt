package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Utfallsperiode
import kotlin.reflect.KClass

interface Vilkårsvurdering {
    fun vurder(periode: Periode): UtfallsperioderForVilkår
    fun fyllInnFaktumDerDetPasser(oppdatertFaktum: Faktum)
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

    private fun nullsafeVurder(faktum: Faktum?, periode: Periode): UtfallsperioderForVilkår =
        faktum?.vurderFor(periode)
            ?: UtfallsperioderForVilkår(
                vilkårForFaktum(),
                listOf(Utfallsperiode(utfall = Utfall.IkkeVurdert, periode = periode))
            )

    private fun vilkårForFaktum(): Vilkår {
        println(clazz.qualifiedName)
        return when (clazz.qualifiedName) {
            KVPFaktum::class.qualifiedName -> KVPVilkår
            Over18Faktum::class.qualifiedName -> Over18Vilkår
            else -> throw IllegalArgumentException("Should have used a sealed class")
        }
    }
}

class KVPVilkårsvurdering : FaktaVilkårsvurdering<KVPFaktum>(KVPFaktum::class)

class Over18Vilkårsvurdering : FaktaVilkårsvurdering<Over18Faktum>(Over18Faktum::class)

class LivsoppholdsytelserVilkårsvurdering(
    private val vilkår: LivsoppholdsytelserVilkår,
    private val kvpVilkårsvurdering: KVPVilkårsvurdering,
    private val over18Vilkårsvurdering: Over18Vilkårsvurdering
) : Vilkårsvurdering {
    override fun vurder(periode: Periode): UtfallsperioderForVilkår {
        return vilkår.akkumuler(listOf(kvpVilkårsvurdering.vurder(periode), over18Vilkårsvurdering.vurder(periode)))
    }

    override fun fyllInnFaktumDerDetPasser(faktum: Faktum) {
        kvpVilkårsvurdering.fyllInnFaktumDerDetPasser(faktum)
        over18Vilkårsvurdering.fyllInnFaktumDerDetPasser(faktum)
    }
}