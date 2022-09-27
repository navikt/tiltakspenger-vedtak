package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.felles.Ulid
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse
import java.time.Duration

@Suppress("TooManyFunctions", "LongParameterList")
class Søker private constructor(
    val id: Ulid,
    val ident: String,
    tilstand: Tilstand,
    søknader: List<Søknad>,
    personopplysninger: Personopplysninger?,
    barn: List<Personopplysninger>,
    tiltak: List<Tiltaksaktivitet>,
    ytelser: List<YtelseSak>,
    val aktivitetslogg: Aktivitetslogg
) : Aktivitetskontekst {
    var tilstand: Tilstand = tilstand
        private set
    var søknader: List<Søknad> = søknader
        private set
    var personopplysninger: Personopplysninger? = personopplysninger
        private set
    var barn: List<Personopplysninger> = barn
        private set
    var tiltak: List<Tiltaksaktivitet> = tiltak
        private set
    var ytelser: List<YtelseSak> = ytelser
        private set

    private val observers = mutableSetOf<SøkerObserver>()

    constructor(
        ident: String
    ) : this(
        id = Ulid.new(SØKER_PREFIX),
        ident = ident,
        tilstand = SøkerRegistrert,
        søknader = mutableListOf(),
        personopplysninger = null,
        barn = mutableListOf(),
        tiltak = mutableListOf(),
        ytelser = mutableListOf(),
        aktivitetslogg = Aktivitetslogg()
    )

    companion object {
        const val SØKER_PREFIX = "SOKER"
        fun fromDb(
            id: Ulid,
            ident: String,
            tilstand: String,
            søknader: List<Søknad>,
        ): Søker {
            return Søker(
                id = id,
                ident = ident,
                tilstand = when (tilstand) {
                    "SøkerRegistrertType" -> SøkerRegistrert
                    "AvventerPersonopplysningerType" -> AvventerPersonopplysninger
                    else -> throw IllegalStateException("Ukjent tilstand $tilstand")
                },
                søknader = søknader,
                personopplysninger = null,
                barn = mutableListOf(),
                tiltak = mutableListOf(),
                ytelser = mutableListOf(),
                aktivitetslogg = Aktivitetslogg(),
            )
        }
    }

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

    fun håndter(personopplysningerMottattHendelse: PersonopplysningerMottattHendelse) {
        if (ident != personopplysningerMottattHendelse.ident()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(personopplysningerMottattHendelse, "Registrert PersonopplysningerMottattHendelse")
        if (erFerdigBehandlet()) {
            personopplysningerMottattHendelse
                .error("ident ${personopplysningerMottattHendelse.ident()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, personopplysningerMottattHendelse)
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

        fun håndter(søker: Søker, personopplysningerMottattHendelse: PersonopplysningerMottattHendelse) {
            personopplysningerMottattHendelse.warn("Forventet ikke PersonopplysningerMottattHendelse i %s", type.name)
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
            søker.søknader += søknadMottattHendelse.søknad()
            søker.trengerPersonopplysninger(søknadMottattHendelse)
            søker.tilstand(søknadMottattHendelse, AvventerPersonopplysninger)
        }
    }

    internal object AvventerPersonopplysninger : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.AvventerPersonopplysningerType
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(søker: Søker, personopplysningerMottattHendelse: PersonopplysningerMottattHendelse) {
            personopplysningerMottattHendelse
                .info("Fikk info om person saker: ${personopplysningerMottattHendelse.personopplysninger()}")
            søker.personopplysninger = personopplysningerMottattHendelse.personopplysninger()
            søker.trengerSkjermingdata(personopplysningerMottattHendelse)
            søker.tilstand(personopplysningerMottattHendelse, AvventerSkjermingdata)
        }
    }

    internal object AvventerSkjermingdata : Tilstand {
        override val type: SøkerTilstandType
            get() = SøkerTilstandType.AvventerSkjermingdataType
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(søker: Søker, skjermingMottattHendelse: SkjermingMottattHendelse) {
            skjermingMottattHendelse.info("Fikk info om skjerming: ${skjermingMottattHendelse.skjerming()}")
            if (søker.personopplysninger == null) {
                skjermingMottattHendelse.severe("Skjerming kan ikke settes når vi ikke har noe Personopplysninger")
            }
            søker.personopplysninger = søker.personopplysninger!!.copy(
                skjermet = skjermingMottattHendelse.skjerming().skjerming
            )
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
            arenaTiltakMottattHendelse
                .info("Fikk info om arenaTiltak: ${arenaTiltakMottattHendelse.tiltaksaktivitet()}")
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

    private fun trengerPersonopplysninger(hendelse: Hendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.personopplysninger,
            melding = "Trenger personopplysninger",
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
//        søknader.accept(visitor)
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
