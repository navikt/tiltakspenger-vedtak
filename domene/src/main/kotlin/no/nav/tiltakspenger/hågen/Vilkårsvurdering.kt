package no.nav.tiltakspenger.hågen

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Personinfo
import no.nav.tiltakspenger.vedtak.meldinger.PersondataMottattHendelse


class Perioder

enum class Kilde {
    SØKNAD, ARENA
}

// TODO: Overhodet ikke ferdig
sealed class Vurdering(val vilkår: Vilkår, val vurderteKilder: List<Kilde>) {
    abstract fun kombinertMed(vurdering: Vurdering, vilkår: Vilkår): Vurdering

    sealed class Avklart(vilkår: Vilkår, vurderteKilder: List<Kilde>) : Vurdering(vilkår, vurderteKilder)
    class IngenKonflikt(vilkår: Vilkår, vurderteKilder: List<Kilde> = emptyList()) : Avklart(vilkår, vurderteKilder) {
        override fun kombinertMed(vurdering: Vurdering, vilkår: Vilkår): Vurdering =
            when (vurdering) {
                is IngenKonflikt -> IngenKonflikt(
                    vilkår = vilkår,
                    vurderteKilder = this.vurderteKilder + vurdering.vurderteKilder
                )
                is Konflikt -> vurdering
                is IkkeImplementert -> TODO()
                is MotstridendeData -> TODO()
                is VenterPåData -> TODO()
            }
    }


    class Konflikt(vilkår: Vilkår, vurderteKilder: List<Kilde>, val perioderIKonflikt: Perioder, val detaljer: String) :
        Avklart(vilkår, vurderteKilder) {
        override fun kombinertMed(vurdering: Vurdering, vilkår: Vilkår): Vurdering {
            return this
            TODO("Not yet implemented")
        }
    }

    sealed class Uavklart(vilkår: Vilkår, vurderteKilder: List<Kilde> = emptyList()) :
        Vurdering(vilkår, vurderteKilder) {
        override fun kombinertMed(vurdering: Vurdering, vilkår: Vilkår): Vurdering {
            return this
            TODO("Not yet implemented")
        }
    }

    class VenterPåData(vilkår: Vilkår, vurderteKilder: List<Kilde> = emptyList()) : Uavklart(vilkår, vurderteKilder)
    class MotstridendeData(vilkår: Vilkår, vurderteKilder: List<Kilde> = emptyList()) : Uavklart(vilkår, vurderteKilder)
    class IkkeImplementert(vilkår: Vilkår, vurderteKilder: List<Kilde> = emptyList()) : Uavklart(vilkår, vurderteKilder)
}

interface Vilkårsvurdering {

    fun vilkår(): Vilkår
    fun håndterHendelse(nyPeriodeHendelse: NyPeriodeHendelse)
    fun trengerManuellVurdering(): Boolean
    fun harMotstridendeData(): Boolean
    fun vurdering(): Vurdering
}


class KVPVilkårsvurdering(periode: Periode) : Vilkårsvurdering {

    var periode: Periode = periode
    var vurdering: Vurdering = Vurdering.IkkeImplementert(vilkår = vilkår())

    override fun trengerManuellVurdering(): Boolean = vurdering is Vurdering.IkkeImplementert
    override fun harMotstridendeData(): Boolean = false
    override fun vilkår() = KVP
    override fun vurdering(): Vurdering = vurdering

    override fun håndterHendelse(nyPeriodeHendelse: NyPeriodeHendelse) {
        periode = nyPeriodeHendelse.nyPeriode()
        vurdering = Vurdering.IkkeImplementert(vilkår = vilkår())
    }

    fun håndterHendelse(kvpManuellVurderingHendelse: KVPManuellVurderingHendelse) {
        vurdering = kvpManuellVurderingHendelse.manuellVurdering()
    }

}

class ErOver18ÅrIVilkårsvurdering(periode: Periode) : Vilkårsvurdering {

    var periode: Periode = periode
    var personinfo: Personinfo? = null
    var vurdering: Vurdering = Vurdering.VenterPåData(vilkår = vilkår())
    override fun trengerManuellVurdering(): Boolean =
        vurdering is Vurdering.IkkeImplementert || vurdering is Vurdering.MotstridendeData

    override fun harMotstridendeData(): Boolean =
        vurdering is Vurdering.MotstridendeData

    override fun vilkår() = ErOver18År
    override fun vurdering(): Vurdering {
        return vurdering
    }

    override fun håndterHendelse(nyPeriodeHendelse: NyPeriodeHendelse) {
        periode = nyPeriodeHendelse.nyPeriode()
        // TODO: Hva hvis man har en manuell vurdering?
        revurder()
    }

    fun håndterHendelse(hendelse: PersondataMottattHendelse) {
        personinfo = hendelse.personinfo()
        // TODO: Hva hvis man har en manuell vurdering?
        revurder()
    }

    fun håndterHendelse(erOver18ÅrManuellVurderingHendelse: ErOver18ÅrManuellVurderingHendelse) {
        vurdering = erOver18ÅrManuellVurderingHendelse.manuellVurdering()
    }

    private fun revurder() {
        // Hvis ikke personinfo er satt blir ikke vurderingen endret
        personinfo?.let { vurdering = it.vurderForPeriode(periode) }
    }

    private fun Personinfo.vurderForPeriode(periode: Periode): Vurdering.Avklart =
        Vurdering.IngenKonflikt(vilkår = vilkår()) //TODO
}

class StatligeYtelserVilkårsvurdering(periode: Periode) : Vilkårsvurdering {

    val kvpVilkårsvurdering: KVPVilkårsvurdering = KVPVilkårsvurdering(periode)
    val erOver18ÅrIVilkårsvurdering: ErOver18ÅrIVilkårsvurdering = ErOver18ÅrIVilkårsvurdering(periode)
    override fun vilkår(): Vilkår = StatligeYtelser

    override fun trengerManuellVurdering(): Boolean =
        kvpVilkårsvurdering.trengerManuellVurdering() || erOver18ÅrIVilkårsvurdering.trengerManuellVurdering()

    override fun harMotstridendeData(): Boolean =
        kvpVilkårsvurdering.harMotstridendeData() || erOver18ÅrIVilkårsvurdering.harMotstridendeData()

    override fun vurdering(): Vurdering =
        kvpVilkårsvurdering.vurdering().kombinertMed(
            vurdering = erOver18ÅrIVilkårsvurdering.vurdering,
            vilkår = StatligeYtelser
        )

    override fun håndterHendelse(nyPeriodeHendelse: NyPeriodeHendelse) {
        kvpVilkårsvurdering.håndterHendelse(nyPeriodeHendelse)
        erOver18ÅrIVilkårsvurdering.håndterHendelse(nyPeriodeHendelse)
    }

}

// TODO: Denne må kunne overstyres på toppnivå, som en sikkerhetsventil hvis ikke logikken vår er korrekt.
class SamletVilkårsvurdering(
    var tiltaksperiode: Periode, //Det er egentlig et tre her også. Kilder er søknad, tiltak og manuell overstyring!
    var erOver18ÅrTotalVilkårsvurdering: ErOver18ÅrIVilkårsvurdering,
    var kvpTotalVilkårsvurdering: KVPVilkårsvurdering,
) {
    fun vurderPeriode(): Perioder {
        var foo = erOver18ÅrTotalVilkårsvurdering.vurdering()
        var bar = kvpTotalVilkårsvurdering.vurdering()
        //regnUtTotalPeriode(foo, bar)
        return Perioder()
    }

    fun kreverManuellBehandling(): List<Vilkårsvurdering> =
        listOf(erOver18ÅrTotalVilkårsvurdering, kvpTotalVilkårsvurdering)
            .filter { it.trengerManuellVurdering() }

    fun håndterHendelse(hendelse: PersondataMottattHendelse) {
        erOver18ÅrTotalVilkårsvurdering.håndterHendelse(hendelse)
    }
}
