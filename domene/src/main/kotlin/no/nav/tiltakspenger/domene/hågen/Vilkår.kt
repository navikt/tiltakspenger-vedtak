package no.nav.tiltakspenger.domene.hågen

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import no.nav.tiltakspenger.domene.Utfallsperiode
import no.nav.tiltakspenger.domene.fakta.Faktum2
import kotlin.reflect.KClass


interface Vilkår2<T> {
    val relevantFaktaType: KClass<*>
    fun vurder(
        faktum: Faktum2<T>,
        vurderingsperiode: Periode
    ): Utfallsperiode //Dette er bare ment for å vise konseptet, jeg tror det må returneres noe annet her
}

interface Vilkårsvurdering2 {
    fun vurder(periode: Periode): Utfallsperiode  //Dette er bare ment for å vise konseptet, jeg tror det må returneres noe annet her

    fun fyllInnFaktumDerDetPasser(oppdatertFaktum: Faktum2<*>)
}

// Her har jeg gjort noe dust, jeg burde bruke det sealed class konseptet dagpenger har lagt opp til i Faktum2 tror jeg..
class KVPFaktumBruker(val deltarKVP: Boolean)

class KVPFaktumSaksbehandler(val deltarKVP: Boolean)

class KVPFaktumRegister(val deltarKVP: Boolean)

object KVPBruker : Vilkår2<KVPFaktumBruker> {
    override val relevantFaktaType = KVPFaktumBruker::class

    override fun vurder(faktum: Faktum2<KVPFaktumBruker>, vurderingsperiode: Periode): Utfallsperiode {
        return when {
            faktum.tilstand == Faktum2.Tilstand.UKJENT -> Utfallsperiode(
                utfall = Utfall.IkkeVurdert,
                periode = vurderingsperiode
            )
            faktum.get().deltarKVP -> Utfallsperiode(
                utfall = Utfall.VurdertOgTrengerManuellBehandling,
                periode = vurderingsperiode
            )
            else -> Utfallsperiode(
                utfall = Utfall.VurdertOgOppfylt,
                periode = vurderingsperiode
            )
        }
    }
}

object KVPSaksbehandler : Vilkår2<KVPFaktumSaksbehandler> {
    override val relevantFaktaType = KVPFaktumSaksbehandler::class

    override fun vurder(faktum: Faktum2<KVPFaktumSaksbehandler>, vurderingsperiode: Periode): Utfallsperiode {
        return when {
            faktum.tilstand == Faktum2.Tilstand.UKJENT -> Utfallsperiode(
                utfall = Utfall.IkkeVurdert,
                periode = vurderingsperiode
            )
            faktum.get().deltarKVP -> Utfallsperiode(
                utfall = Utfall.VurdertOgIkkeOppfylt,
                periode = vurderingsperiode
            )
            else -> Utfallsperiode(
                utfall = Utfall.VurdertOgOppfylt,
                periode = vurderingsperiode
            )
        }
    }
}

object KVPRegister : Vilkår2<KVPFaktumRegister> {
    override val relevantFaktaType = KVPFaktumSaksbehandler::class

    override fun vurder(faktum: Faktum2<KVPFaktumRegister>, vurderingsperiode: Periode): Utfallsperiode {
        return when {
            faktum.tilstand == Faktum2.Tilstand.UKJENT -> Utfallsperiode(
                utfall = Utfall.IkkeVurdert,
                periode = vurderingsperiode
            )
            faktum.get().deltarKVP -> Utfallsperiode(
                utfall = Utfall.VurdertOgIkkeOppfylt,
                periode = vurderingsperiode
            )
            else -> Utfallsperiode(
                utfall = Utfall.VurdertOgOppfylt,
                periode = vurderingsperiode
            )
        }
    }
}

abstract class LøvVilkårsvurdering<T>(
    private val vilkår: Vilkår2<T>,
    private var faktum: Faktum2<T> = Faktum2(
        tilstand = Faktum2.Tilstand.UKJENT,
        verdi = null
    )
) : Vilkårsvurdering2 {

    override fun fyllInnFaktumDerDetPasser(oppdatertFaktum: Faktum2<*>) {
        //Dette fungerer ikke. Jeg hater generics.. Hjelper det hvis jeg gjeninnfører sealed på Faktum2?
        if (vilkår.relevantFaktaType.isInstance(oppdatertFaktum)) {
            faktum = oppdatertFaktum as Faktum2<T>
        }
    }

    override fun vurder(periode: Periode): Utfallsperiode =
        vilkår.vurder(faktum = faktum, vurderingsperiode = periode)
}

class KVPVBrukerVilkårsvurdering : LøvVilkårsvurdering<KVPFaktumBruker>(KVPBruker)
class KVPSaksbehandlerVilkårsvurdering : LøvVilkårsvurdering<KVPFaktumSaksbehandler>(KVPSaksbehandler)
class KVPRegisterVilkårsvurdering : LøvVilkårsvurdering<KVPFaktumRegister>(KVPRegister)

abstract class HolmgangVilkårsvurdering(
    private val vilkårsvurderingBruker: Vilkårsvurdering2,
    private val vilkårsvurderingRegister: Vilkårsvurdering2,
    private val vilkårsvurderingSaksbehandler: Vilkårsvurdering2
) : Vilkårsvurdering2 {

    override fun fyllInnFaktumDerDetPasser(oppdatertFaktum: Faktum2<*>) {
        vilkårsvurderingBruker.fyllInnFaktumDerDetPasser(oppdatertFaktum)
        vilkårsvurderingRegister.fyllInnFaktumDerDetPasser(oppdatertFaktum)
        vilkårsvurderingSaksbehandler.fyllInnFaktumDerDetPasser(oppdatertFaktum)
    }

    override fun vurder(periode: Periode): Utfallsperiode {
        return holmgang(
            vilkårsvurderingBruker.vurder(periode),
            vilkårsvurderingRegister.vurder(periode),
            vilkårsvurderingSaksbehandler.vurder(periode)
        )
    }

    open fun holmgang(
        vurderFaktum1: Utfallsperiode,
        vurderFaktum2: Utfallsperiode,
        vurderFaktum3: Utfallsperiode
    ): Utfallsperiode {
        //Default implementasjon, kan overrides i konkrete tilfeller
        //Denne er åpenbart bare vås..
        return Utfallsperiode(Utfall.VurdertOgOppfylt, vurderFaktum1.periode)
    }
}

class KVPVilkårsvurdering(
    vilkårsvurderingBruker: KVPVBrukerVilkårsvurdering,
    vilkårsvurderingRegister: KVPRegisterVilkårsvurdering,
    vilkårsvurderingSaksbehandler: KVPSaksbehandlerVilkårsvurdering
) : HolmgangVilkårsvurdering(vilkårsvurderingBruker, vilkårsvurderingRegister, vilkårsvurderingSaksbehandler)

/*
class VilkårsvurderingBuilder()

infix fun Periode.vurder(vilkår: Vilkår): Vilkårsvurdering = Vilkårsvurdering(vilkår = vilkår, vurderingsperiode = this)
infix fun Vilkårsvurdering.medFaktum(faktum: Faktum): Vilkårsvurdering = this.vurder(faktum)

fun test() {
    val vurderingsperiode = Periode(LocalDate.now(), LocalDate.now())
    val vilkårsvurdering = vurderingsperiode vurder ErOver18År medFaktum FødselsdatoFaktum(
        kilde = FaktumKilde.BRUKER,
        fødselsdato = LocalDate.now()
    )
}

 */