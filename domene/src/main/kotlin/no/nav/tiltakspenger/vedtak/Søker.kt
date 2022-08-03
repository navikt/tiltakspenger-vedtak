package no.nav.tiltakspenger.vedtak

import java.time.Duration
import no.nav.tiltakspenger.vedtak.meldinger.PersondataMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse

class Søker private constructor(
    private val ident: String,
    private var tilstand: Tilstand,
    private var søknader: MutableList<Søknad>,
    private var personinfo: MutableList<Personinfo>,
    internal val aktivitetslogg: Aktivitetslogg
) : Aktivitetskontekst {
    private val observers = mutableSetOf<SøkerObserver>()

    constructor(
        ident: String
    ) : this(
        ident = ident,
        tilstand = SøkerRegistrert,
        søknader = mutableListOf<Søknad>(),
        personinfo = mutableListOf<Personinfo>(),
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
            //Dette er for simpelt, må sjekke om den skal lagres
            søker.søknader.add(søknadMottattHendelse.søknad())
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
            //Dette er for simpelt, må sjekke om den skal lagres
            søker.personinfo.add(persondataMottattHendelse.personinfo())
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
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.Persondata,
            melding = "Trenger persondata",
            detaljer = mapOf("identer" to listOf(this.ident))
        )
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
        søknader.forEach { it.accept(visitor) }
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