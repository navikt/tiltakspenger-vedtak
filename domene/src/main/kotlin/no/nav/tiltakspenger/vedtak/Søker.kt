package no.nav.tiltakspenger.vedtak

import java.time.Duration
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersondataMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse

class Søker private constructor(
    private val ident: String,
    private var tilstand: Tilstand,
    private var søknad: Søknad?,
    private var personinfo: Personinfo?,
    private var tiltak: List<Tiltaksaktivitet>,
    private var ytelser: List<YtelseSak>,
    private var skjerming: Boolean?,
    internal val aktivitetslogg: Aktivitetslogg
) : Aktivitetskontekst {
    private val observers = mutableSetOf<SøkerObserver>()

    constructor(
        ident: String
    ) : this(
        ident = ident,
        tilstand = SøkerRegistrert,
        søknad = null,
        personinfo = null,
        tiltak = mutableListOf(),
        ytelser = mutableListOf(),
        skjerming = null,
        aktivitetslogg = Aktivitetslogg()
    )

    fun ident(): String = ident

    fun håndter(søknadMottattHendelse: SøknadMottattHendelse) {
        if (ident != søknadMottattHendelse.ident()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(søknadMottattHendelse, "Registrert SøknadMottattHendelse")
        if (erFerdigBehandlet()) {
            søknadMottattHendelse.error("ident ${søknadMottattHendelse.ident()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, søknadMottattHendelse)
    }

    fun håndter(persondataMottattHendelse: PersondataMottattHendelse) {
        if (ident != persondataMottattHendelse.ident()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(persondataMottattHendelse, "Registrert PersondataMottattHendelse")
        if (erFerdigBehandlet()) {
            persondataMottattHendelse.error("ident ${persondataMottattHendelse.ident()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, persondataMottattHendelse)
    }

    fun håndter(skjermingMottattHendelse: SkjermingMottattHendelse) {
        if (ident != skjermingMottattHendelse.ident()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(skjermingMottattHendelse, "Registrert SkjermingMottattHendelse")
        if (erFerdigBehandlet()) {
            skjermingMottattHendelse.error("ident ${skjermingMottattHendelse.ident()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, skjermingMottattHendelse)
    }

    fun håndter(arenaTiltakMottattHendelse: ArenaTiltakMottattHendelse) {
        if (ident != arenaTiltakMottattHendelse.ident()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(arenaTiltakMottattHendelse, "Registrert ArenaTiltakMottattHendelse")
        if (erFerdigBehandlet()) {
            arenaTiltakMottattHendelse.error("ident ${arenaTiltakMottattHendelse.ident()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, arenaTiltakMottattHendelse)
    }

    fun håndter(ytelserMottattHendelse: YtelserMottattHendelse) {
        if (ident != ytelserMottattHendelse.ident()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(ytelserMottattHendelse, "Registrert YtelserMottattHendelse")
        if (erFerdigBehandlet()) {
            ytelserMottattHendelse.error("ident ${ytelserMottattHendelse.ident()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, ytelserMottattHendelse)
    }

    private fun kontekst(hendelse: Hendelse, melding: String) {
        hendelse.setForelderAndAddKontekst(this)
        hendelse.addKontekst(this.tilstand)
        hendelse.info(melding)
    }

    // Gang of four State pattern
    interface Tilstand : Aktivitetskontekst {
        val type: SøkerTilstandType
        val timeout: Duration

        fun håndter(søker: Søker, søknadMottattHendelse: SøknadMottattHendelse) {
            søknadMottattHendelse.warn("Forventet ikke SøknadMottattHendelse i %s", type.name)
        }

        fun håndter(søker: Søker, persondataMottattHendelse: PersondataMottattHendelse) {
            persondataMottattHendelse.warn("Forventet ikke PersondataMottattHendelse i %s", type.name)
        }

        fun håndter(søker: Søker, skjermingMottattHendelse: SkjermingMottattHendelse) {
            skjermingMottattHendelse.warn("Forventet ikke SkjermingMottattHendelse i %s", type.name)
        }

        fun håndter(søker: Søker, arenaTiltakMottattHendelse: ArenaTiltakMottattHendelse) {
            arenaTiltakMottattHendelse.warn("Forventet ikke ArenaTiltakMottattHendelse i %s", type.name)
        }

        fun håndter(søker: Søker, ytelserMottattHendelse: YtelserMottattHendelse) {
            ytelserMottattHendelse.warn("Forventet ikke YtelserMottattHendelse i %s", type.name)
        }

        fun leaving(søker: Søker, hendelse: Hendelse) {}
        fun entering(søker: Søker, hendelse: Hendelse) {}

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            return SpesifikkKontekst(
                "Tilstand",
                mapOf(
                    "tilstandtype" to type.name
                )
            )
        }
    }

    internal object SøkerRegistrert : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.SøkerRegistrertType
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(søker: Søker, søknadMottattHendelse: SøknadMottattHendelse) {
            søker.søknad = søknadMottattHendelse.søknad()
            søker.trengerPersondata(søknadMottattHendelse)
            søker.tilstand(søknadMottattHendelse, AvventerPersondata)
        }
    }

    internal object AvventerPersondata : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.AvventerPersondataType
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(søker: Søker, persondataMottattHendelse: PersondataMottattHendelse) {
            persondataMottattHendelse.info("Fikk info om person saker: ${persondataMottattHendelse.personinfo()}")
            søker.personinfo = persondataMottattHendelse.personinfo()
            søker.trengerSkjermingdata(persondataMottattHendelse)
            søker.tilstand(persondataMottattHendelse, AvventerSkjermingdata)
        }
    }

    internal object AvventerSkjermingdata : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.AvventerSkjermingdataType
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(søker: Søker, skjermingMottattHendelse: SkjermingMottattHendelse) {
            skjermingMottattHendelse.info("Fikk info om skjerming: ${skjermingMottattHendelse.skjerming()}")
            søker.skjerming = skjermingMottattHendelse.skjerming().skjerming
            søker.trengerTiltak(skjermingMottattHendelse)
            søker.tilstand(skjermingMottattHendelse, AvventerTiltak)
        }
    }

    internal object AvventerTiltak : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.AvventerTiltak
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(søker: Søker, arenaTiltakMottattHendelse: ArenaTiltakMottattHendelse) {
            arenaTiltakMottattHendelse.info("Fikk info om arenaTiltak: ${arenaTiltakMottattHendelse.tiltaksaktivitet()}")
            søker.tiltak = arenaTiltakMottattHendelse.tiltaksaktivitet()
            søker.trengerArenaYtelse(arenaTiltakMottattHendelse)
            søker.tilstand(arenaTiltakMottattHendelse, AvventerYtelser)
        }
    }

    internal object AvventerYtelser : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.AvventerYtelser
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(søker: Søker, ytelserMottattHendelse: YtelserMottattHendelse) {
            ytelserMottattHendelse.info("Fikk info om arenaYtelser: ${ytelserMottattHendelse.ytelseSak()}")
            søker.ytelser = ytelserMottattHendelse.ytelseSak()
            søker.tilstand(ytelserMottattHendelse, SøkerFerdigstiltType)
        }
    }

    internal object SøkerFerdigstiltType : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.SøkerFerdigstiltType
        override val timeout: Duration
            get() = Duration.ofDays(1)

    }

    private fun trengerPersondata(hendelse: Hendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.persondata,
            melding = "Trenger persondata",
            detaljer = mapOf("ident" to this.ident)
        )
    }

    private fun trengerSkjermingdata(hendelse: Hendelse) {
        hendelse.behov(Aktivitetslogg.Aktivitet.Behov.Behovtype.skjerming, "Trenger skjermingdata")
    }

    private fun trengerTiltak(hendelse: Hendelse) {
        hendelse.behov(Aktivitetslogg.Aktivitet.Behov.Behovtype.arenatiltak, "Trenger arenatiltak")
    }

    private fun trengerArenaYtelse(hendelse: Hendelse) {
        hendelse.behov(Aktivitetslogg.Aktivitet.Behov.Behovtype.arenaytelser, "Trenger arenaytelser")
    }

    private fun tilstand(
        event: Hendelse,
        nyTilstand: Tilstand,
        block: () -> Unit = {}
    ) {
        if (tilstand == nyTilstand) {
            return // Already in this state => ignore
        }
        tilstand.leaving(this, event)
        val previousState = tilstand
        tilstand = nyTilstand
        block()
        event.addKontekst(tilstand)
        emitTilstandEndret(tilstand.type, event.aktivitetslogg, previousState.type, tilstand.timeout)
        tilstand.entering(this, event)
    }

    private fun emitTilstandEndret(
        gjeldendeTilstand: SøkerTilstandType,
        aktivitetslogg: Aktivitetslogg,
        forrigeTilstand: SøkerTilstandType,
        timeout: Duration
    ) {
        observers.forEach {
            it.tilstandEndret(
                SøkerObserver.SøkerEndretTilstandEvent(
                    ident = ident,
                    gjeldendeTilstand = gjeldendeTilstand,
                    forrigeTilstand = forrigeTilstand,
                    aktivitetslogg = aktivitetslogg,
                    timeout = timeout
                )
            )
        }
    }

    //Har jeg kåla det til her? Når bruker man accept og når bruker man visit??
    fun accept(visitor: SøkerVisitor) {
        visitor.preVisitSøker(this, ident)
        visitor.visitTilstand(tilstand)
        //journalpost?.accept(visitor)
        søknad?.accept(visitor)
        visitor.visitSøkerAktivitetslogg(aktivitetslogg)
        aktivitetslogg.accept(visitor)
        visitor.postVisitSøker(this, ident)
    }

    fun addObserver(observer: SøkerObserver) {
        observers.add(observer)
    }

    private fun erFerdigBehandlet() =
        this.tilstand.type in setOf(
            SøkerTilstandType.SøkerFerdigstiltType,
            SøkerTilstandType.AlleredeBehandletType
        )

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst(
        "Søker",
        mapOf(
            "ident" to ident
        )
    )

    // Jeg har fjernet flere av
    // private fun emit* funksjonene
}
