package no.nav.tiltakspenger.vedtak

import java.time.Duration
import no.nav.tiltakspenger.vedtak.meldinger.PersondataMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse

class Søker private constructor(
    private val ident: String,
    private var tilstand: Tilstand,
    private var søknad: Søknad?,
    private var person: Person?,
    internal val aktivitetslogg: Aktivitetslogg
) : Aktivitetskontekst {
    private val observers = mutableSetOf<SøkerObserver>()

    constructor(
        ident: String
    ) : this(
        ident = ident,
        tilstand = PersonRegistrert,
        søknad = null,
        person = null,
        aktivitetslogg = Aktivitetslogg()
    )

    fun ident(): String = ident

    fun håndter(søknadMottattHendelse: SøknadMottattHendelse) {
        if (ident != søknadMottattHendelse.ident()) return
        kontekst(søknadMottattHendelse, "Registrert SøknadMottattHendelse")
        if (erFerdigBehandlet()) {
            søknadMottattHendelse.error("ident ${søknadMottattHendelse.ident()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, søknadMottattHendelse)
    }

    private fun kontekst(hendelse: Hendelse, melding: String) {
        hendelse.kontekst(this)
        hendelse.kontekst(this.tilstand)
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

        fun leaving(søker: Søker, hendelse: Hendelse) {}
        fun entering(søker: Søker, hendelse: Hendelse) {}

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            return SpesifikkKontekst(
                "Tilstand",
                mapOf(
                    "tilstand" to type.name
                )
            )
        }
    }

    internal object PersonRegistrert : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.PersonRegistrertType
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
            persondataMottattHendelse.info("Fikk info om person saker: ${persondataMottattHendelse.person()}")
            søker.person = persondataMottattHendelse.person()
            søker.trengerSkjermingdata(persondataMottattHendelse)
            søker.tilstand(persondataMottattHendelse, AvventerSkjermingdata)
        }
    }

    internal object AvventerSkjermingdata : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.AvventerSkjermingdataType
        override val timeout: Duration
            get() = Duration.ofDays(1)

        //Må override håndter denne også, osv osv..
    }

    private fun trengerPersondata(hendelse: Hendelse) {
        hendelse.behov(Aktivitetslogg.Aktivitet.Behov.Behovtype.Persondata, "Trenger persondata")
    }

    private fun trengerSkjermingdata(hendelse: Hendelse) {
        hendelse.behov(Aktivitetslogg.Aktivitet.Behov.Behovtype.Skjermingdata, "Trenger skjermingdata")
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
        event.kontekst(tilstand)
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