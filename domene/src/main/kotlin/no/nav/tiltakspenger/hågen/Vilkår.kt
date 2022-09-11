package no.nav.tiltakspenger.hågen

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.Personinfo
import no.nav.tiltakspenger.vedtak.meldinger.PersondataMottattHendelse

interface Vilkår {
    fun paragraf(): String
}

interface Inngangsvilkår : Vilkår

// Hva er evt fordelen ved å  ha det som objects?
enum class InngangsvilkårEnum(private val paragraf: String) : Inngangsvilkår {
    ErOver18År("7.1"),
    KVP("7.3");

    override fun paragraf(): String = paragraf
}

object ErOver18År : Inngangsvilkår {
    override fun paragraf(): String = "7.1"
}

object KVP : Inngangsvilkår {
    override fun paragraf(): String = "7.3"
}

object StatligeYtelser : Inngangsvilkår {
    override fun paragraf(): String = "7"
}

class Perioder

enum class Kilde {
    SØKNAD, ARENA
}

// TODO: Overhodet ikke ferdig
sealed class Vurdering {
    abstract fun kombinertMed(vurdering: Vurdering): Vurdering

    sealed class Avklart : Vurdering()
    class IngenKonflikt(val vurderteKilder: List<Kilde> = emptyList()) : Avklart() {
        override fun kombinertMed(vurdering: Vurdering): Vurdering =
            when (vurdering) {
                is IngenKonflikt -> IngenKonflikt(vurderteKilder = this.vurderteKilder + vurdering.vurderteKilder)
                is Konflikt -> vurdering
                is IkkeImplementert -> TODO()
                is MotstridendeData -> TODO()
                is VenterPåData -> TODO()
            }
    }


    class Konflikt(val perioderIKonflikt: Perioder, val vurderteKilder: Kilde, val detaljer: String) : Avklart() {
        override fun kombinertMed(vurdering: Vurdering): Vurdering {
            TODO("Not yet implemented")
            return this
        }
    }

    sealed class Uavklart : Vurdering() {
        override fun kombinertMed(vurdering: Vurdering): Vurdering {
            TODO("Not yet implemented")
            return this
        }
    }

    class VenterPåData : Uavklart()
    class MotstridendeData(val vurderteKilder: List<Kilde>) : Uavklart()
    class IkkeImplementert : Uavklart()
}

interface Vilkårsvurdering {

    fun vilkår(): Vilkår
    fun håndterHendelse(nyPeriodeHendelse: NyPeriodeHendelse)
    fun trengerManuellVurdering(): Boolean
    fun harMotstridendeData(): Boolean
    fun vurdering(): Vurdering
}

class KVPManuellVurderingHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val manuellVurdering: Vurdering,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun manuellVurdering() = manuellVurdering
}

class ErOver18ÅrManuellVurderingHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val manuellVurdering: Vurdering,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun manuellVurdering() = manuellVurdering
}

class NyPeriodeHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val nyPeriode: Periode,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun nyPeriode() = nyPeriode
}

class KVPVilkårsvurdering(periode: Periode) : Vilkårsvurdering {

    var periode: Periode = periode
    var vurdering: Vurdering = Vurdering.IkkeImplementert()

    override fun trengerManuellVurdering(): Boolean = vurdering is Vurdering.IkkeImplementert
    override fun harMotstridendeData(): Boolean = false
    override fun vilkår() = KVP
    override fun vurdering(): Vurdering = vurdering

    override fun håndterHendelse(nyPeriodeHendelse: NyPeriodeHendelse) {
        periode = nyPeriodeHendelse.nyPeriode()
        vurdering = Vurdering.IkkeImplementert()
    }

    fun håndterHendelse(kvpManuellVurderingHendelse: KVPManuellVurderingHendelse) {
        vurdering = kvpManuellVurderingHendelse.manuellVurdering()
    }

}

class ErOver18ÅrIVilkårsvurdering(periode: Periode) : Vilkårsvurdering {

    var periode: Periode = periode
    var personinfo: Personinfo? = null
    var vurdering: Vurdering = Vurdering.VenterPåData()
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

    private fun Personinfo.vurderForPeriode(periode: Periode): Vurdering.Avklart = Vurdering.IngenKonflikt() //TODO
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
        kvpVilkårsvurdering.vurdering().kombinertMed(erOver18ÅrIVilkårsvurdering.vurdering)

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


// Noen hendelser vil ha effekt litt på kryss og tvers. Hvis man f.eks endrer tiltaksperioden, så må alle manuelle
// vurderinger gjøres på nytt. Kan håndteres vha en Observatør eller ved å la Eventen boble nedover overalt?

class Førstegangsbehandling(val samletVilkårsvurdering: SamletVilkårsvurdering) {


}

class Revurdering(var forrigeVurdering: SamletVilkårsvurdering, var endredeVilkårsvurderinger: List<Vilkårsvurdering>) {
    fun endreVurdering(vilkårsvurdering: Vilkårsvurdering) {}
}
