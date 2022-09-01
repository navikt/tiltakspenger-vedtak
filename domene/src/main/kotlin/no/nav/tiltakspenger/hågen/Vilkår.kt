package no.nav.tiltakspenger.hågen

import no.nav.tiltakspenger.domene.Periode
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

class Perioder

interface Vilkårsvurdering {
    fun vilkår(): Vilkår
    fun perioderDiskvalifisertForTiltakspenger(tiltaksperiode: Periode): Perioder
}

interface TotalVilkårsvurdering : Vilkårsvurdering {
    fun trengerManuellVurdering(): Boolean
    fun harMotstridendeData(): Boolean
}

class KVPManuellVilkårsvurdering(private var manueltVurderteDiskvalifisertePerioder: Perioder) : Vilkårsvurdering {
    override fun vilkår() = KVP
    override fun perioderDiskvalifisertForTiltakspenger(tiltaksperiode: Periode): Perioder =
        manueltVurderteDiskvalifisertePerioder
}

class KVPTotalVilkårsvurdering(
    private var kvpManuellVilkårsvurdering: KVPManuellVilkårsvurdering?
) : TotalVilkårsvurdering {
    override fun trengerManuellVurdering(): Boolean = kvpManuellVilkårsvurdering == null

    override fun harMotstridendeData(): Boolean = false

    override fun vilkår() = KVP
    override fun perioderDiskvalifisertForTiltakspenger(tiltaksperiode: Periode): Perioder =
        kvpManuellVilkårsvurdering?.perioderDiskvalifisertForTiltakspenger(tiltaksperiode)
            ?: Perioder()
}

class ErOver18ÅrPdlVilkårsvurdering(private var personinfo: Personinfo) : Vilkårsvurdering {
    override fun vilkår() = ErOver18År
    override fun perioderDiskvalifisertForTiltakspenger(tiltaksperiode: Periode): Perioder = Perioder()
}

class ErOver18ÅrManuellVilkårsvurdering(private var manueltVurderteDiskvalifisertePerioder: Perioder) :
    Vilkårsvurdering {
    override fun vilkår() = ErOver18År
    override fun perioderDiskvalifisertForTiltakspenger(tiltaksperiode: Periode): Perioder =
        manueltVurderteDiskvalifisertePerioder
}

class ErOver18ÅrTotalVilkårsvurdering(
    var erOver18ÅrPdlVilkårsvurdering: ErOver18ÅrPdlVilkårsvurdering?,
    var erOver18ÅrManuellVilkårsvurdering: ErOver18ÅrManuellVilkårsvurdering?,
) : TotalVilkårsvurdering {
    override fun trengerManuellVurdering(): Boolean =
        erOver18ÅrManuellVilkårsvurdering == null && erOver18ÅrPdlVilkårsvurdering == null

    override fun harMotstridendeData(): Boolean = false

    override fun vilkår() = ErOver18År
    override fun perioderDiskvalifisertForTiltakspenger(tiltaksperiode: Periode): Perioder {
        return erOver18ÅrManuellVilkårsvurdering?.perioderDiskvalifisertForTiltakspenger(tiltaksperiode)
            ?: erOver18ÅrPdlVilkårsvurdering?.perioderDiskvalifisertForTiltakspenger(tiltaksperiode)
            ?: Perioder()
    }

    fun håndterHendelse(hendelse: PersondataMottattHendelse) {
        // TODO: Kanskje det f.eks er aktuelt å nulle ut en eldre manuell vurdering fordi det har kommet inn nyere data?
        // TODO: Bør sjekke om dataene vi mottar faktisk er nyere enn de vi har fra før?
        erOver18ÅrPdlVilkårsvurdering = ErOver18ÅrPdlVilkårsvurdering(hendelse.personinfo())
    }
}

class SamletVilkårsvurdering(
    var tiltaksperiode: Periode,
    var erOver18ÅrTotalVilkårsvurdering: ErOver18ÅrTotalVilkårsvurdering,
    var kvpTotalVilkårsvurdering: KVPTotalVilkårsvurdering,
) {
    fun vurderPeriode(): Perioder {
        var foo = erOver18ÅrTotalVilkårsvurdering.perioderDiskvalifisertForTiltakspenger(tiltaksperiode)
        var bar = kvpTotalVilkårsvurdering.perioderDiskvalifisertForTiltakspenger(tiltaksperiode)
        //regnUtTotalPeriode(foo, bar)
        return Perioder()
    }

    fun kreverManuellBehandling(): List<TotalVilkårsvurdering> =
        listOf(erOver18ÅrTotalVilkårsvurdering, kvpTotalVilkårsvurdering)
            .filter { it.trengerManuellVurdering() }

    fun håndterHendelse(hendelse: PersondataMottattHendelse) {
        erOver18ÅrTotalVilkårsvurdering.håndterHendelse(hendelse)
    }
}

enum class Kilde {
    SØKNAD, ARENA
}

// TODO: Overhodet ikke ferdig
sealed class TotalVurdering {
    class IngenKonflikt(val underliggendeVurderinger: List<Vurdering>)
    class Konflikt(val perioderIKonflikt: Perioder, val kilde: Kilde, val detaljer: String)
    class UavklartVenterPaaData
    class UavklartTrengerManuellVurdering
    class UavklartMotstridendeData(val underliggendeVurderinger: List<Vurdering>)
}

sealed class Vurdering {
    class IngenKonflikt(val kilde: Kilde, val detaljer: String)
    class Konflikt(val perioderIKonflikt: Perioder, val kilde: Kilde, val detaljer: String)
    class UavklartVenterPaaData
}

// Noen hendelser vil ha effekt litt på kryss og tvers. Hvis man f.eks endrer tiltaksperioden, så må alle manuelle
// vurderinger gjøres på nytt. Kan håndteres vha en Observatør eller ved å la Eventen boble nedover overalt?
