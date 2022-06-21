package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.vedtak.meldinger.JoarkHendelse
import java.time.Duration

class Innsending private constructor(
    private val journalpostId: String,
    private var tilstand: Tilstand,
    //private var journalpost: Journalpost?,
    //private var søknad: Søknadsdata.Søknad?,
    private var oppfyllerMinsteArbeidsinntekt: Boolean?,
    private var eksisterendeSaker: Boolean?,
    //private var person: Person?,
    //private var arenaSak: ArenaSak?,
    internal val aktivitetslogg: Aktivitetslogg
) : Aktivitetskontekst {
    private val observers = mutableSetOf<InnsendingObserver>()

    constructor(
        journalpostId: String
    ) : this(
        journalpostId = journalpostId,
        tilstand = Mottatt,
        //journalpost = null,
        //søknad = null,
        oppfyllerMinsteArbeidsinntekt = null,
        eksisterendeSaker = null,
        //person = null,
        //arenaSak = null,
        aktivitetslogg = Aktivitetslogg()
    )

    fun journalpostId(): String = journalpostId

    fun håndter(joarkHendelse: JoarkHendelse) {
        if (journalpostId != joarkHendelse.journalpostId()) return
        kontekst(joarkHendelse, "Registrert joark hendelse")
        if (erFerdigBehandlet()) {
            joarkHendelse.error("Journalpost med id ${joarkHendelse.journalpostId()} allerede ferdig behandlet")
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

        fun håndter(innsending: Innsending, joarkHendelse: JoarkHendelse) {
            joarkHendelse.warn("Forventet ikke JoarkHendelse i %s", type.name)
        }

        fun leaving(innsending: Innsending, hendelse: Hendelse) {}
        fun entering(innsending: Innsending, hendelse: Hendelse) {}

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

        override fun håndter(innsending: Innsending, joarkHendelse: JoarkHendelse) {
            innsending.trengerJournalpost(joarkHendelse)
            innsending.tilstand(joarkHendelse, AvventerJournalpost)
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
                InnsendingObserver.InnsendingEndretTilstandEvent(
                    journalpostId = journalpostId,
                    gjeldendeTilstand = gjeldendeTilstand,
                    forrigeTilstand = forrigeTilstand,
                    aktivitetslogg = aktivitetslogg,
                    timeout = timeout
                )
            )
        }
    }

    fun accept(visitor: InnsendingVisitor) {
        visitor.preVisitInnsending(this, journalpostId)
        visitor.visitTilstand(tilstand)
        visitor.visitInnsending(oppfyllerMinsteArbeidsinntekt, eksisterendeSaker)
        //journalpost?.accept(visitor)
        //arenaSak?.accept(visitor)
        //person?.accept(visitor)
        //søknad?.accept(visitor)
        visitor.visitInnsendingAktivitetslogg(aktivitetslogg)
        aktivitetslogg.accept(visitor)
        visitor.postVisitInnsending(this, journalpostId)
    }

    fun addObserver(observer: InnsendingObserver) {
        observers.add(observer)
    }

    private fun erFerdigBehandlet() =
        this.tilstand.type in setOf(
            InnsendingTilstandType.InnsendingFerdigstiltType,
            InnsendingTilstandType.AlleredeBehandletType
        )

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst(
        "Innsending",
        mapOf(
            "journalpostId" to journalpostId
        )
    )

    // Jeg har fjernet
    // private fun emit* funksjonene
}