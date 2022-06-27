package no.nav.tiltakspenger.vedtak

import java.time.Duration
import no.nav.tiltakspenger.Søknad
import no.nav.tiltakspenger.vedtak.meldinger.JoarkHendelse

class Søker private constructor(
    private val ident: String,
    private var tilstand: Tilstand,
    //private var journalpost: Journalpost?,
    private var søknad: Søknad?,
    internal val aktivitetslogg: Aktivitetslogg
) : Aktivitetskontekst {
    private val observers = mutableSetOf<SøkerObserver>()

    constructor(
        ident: String
    ) : this(
        ident = ident,
        tilstand = Mottatt,
        //journalpost = null,
        søknad = null,
        aktivitetslogg = Aktivitetslogg()
    )

    fun ident(): String = ident

    fun håndter(joarkHendelse: JoarkHendelse) {
        if (ident != joarkHendelse.ident()) return
        kontekst(joarkHendelse, "Registrert joark hendelse")
        if (erFerdigBehandlet()) {
            joarkHendelse.error("ident  ${joarkHendelse.ident()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, joarkHendelse)
    }

    private fun kontekst(hendelse: Hendelse, melding: String) {
        hendelse.kontekst(this)
        hendelse.kontekst(this.tilstand)
        hendelse.info(melding)
    }

    // Gang of four State pattern
    interface Tilstand : Aktivitetskontekst {
        val type: InnsendingTilstandType
        val timeout: Duration

        fun håndter(søker: Søker, joarkHendelse: JoarkHendelse) {
            joarkHendelse.warn("Forventet ikke JoarkHendelse i %s", type.name)
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

    internal object Mottatt : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.MottattType
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(søker: Søker, joarkHendelse: JoarkHendelse) {
            søker.trengerJournalpost(joarkHendelse)
            søker.tilstand(joarkHendelse, AvventerJournalpost)
        }
    }

    internal object AvventerJournalpost : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerJournalpostType
        override val timeout: Duration
            get() = Duration.ofDays(1)
    }

    private fun trengerJournalpost(hendelse: Hendelse) {
        hendelse.behov(Aktivitetslogg.Aktivitet.Behov.Behovtype.Journalpost, "Trenger journalpost")
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
        gjeldendeTilstand: InnsendingTilstandType,
        aktivitetslogg: Aktivitetslogg,
        forrigeTilstand: InnsendingTilstandType,
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
            InnsendingTilstandType.InnsendingFerdigstiltType,
            InnsendingTilstandType.AlleredeBehandletType
        )

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst(
        "Søker",
        mapOf(
            "ident" to ident
        )
    )

    // Jeg har fjernet
    // private fun emit* funksjonene
}