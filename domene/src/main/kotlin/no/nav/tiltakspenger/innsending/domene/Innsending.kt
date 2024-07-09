package no.nav.tiltakspenger.innsending.domene

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.DomeneMetrikker
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.innsending.domene.helper.DirtyCheckingAktivitetslogg
import no.nav.tiltakspenger.innsending.domene.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.InnsendingUtdatertHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.ResetInnsendingHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.innsending.domene.meldinger.TiltakMottattHendelse
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.barnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.barnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.søkerOrNull
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class Innsending private constructor(
    val id: InnsendingId,
    val journalpostId: String,
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate,
    tilstand: Tilstand,
    sistEndret: LocalDateTime?,
    personopplysninger: InnhentedePersonopplysninger?,
    aktivitetslogg: Aktivitetslogg,
) : KontekstLogable {
    private val dirtyChecker: DirtyChecker = DirtyChecker()

    val aktivitetslogg: IAktivitetslogg =
        DirtyCheckingAktivitetslogg(aktivitetslogg, dirtyChecker.get("aktivitetslogg"))

    fun isDirty() = dirtyChecker.isDirty()

    fun endringsHash(): String =
        listOfNotNull(
            personopplysninger?.tidsstempelInnhentet,
        ).fold(journalpostId.hashCode()) { hash, tidsstempel -> 31 * hash + tidsstempel.hashCode() }.toString()

    var tilstand: Tilstand = tilstand
        private set(value) {
            field = value
            dirtyChecker.set("tilstand")
        }

    var personopplysninger: InnhentedePersonopplysninger? = personopplysninger
        private set(value) {
            field = value
            dirtyChecker.set("personopplysninger")
        }

    var sistEndret: LocalDateTime? = sistEndret
        private set

    private val observers = mutableSetOf<InnsendingObserver>()

    fun personopplysningerSøker() =
        personopplysninger?.personopplysningerliste?.søkerOrNull()

    fun personopplysningerBarnUtenIdent() =
        personopplysninger?.personopplysningerliste?.barnUtenIdent() ?: emptyList()

    private fun personopplysningerBarnMedIdent() =
        personopplysninger?.personopplysningerliste?.barnMedIdent() ?: emptyList()

    fun oppdaterSistEndret(sistEndret: LocalDateTime) {
        this.sistEndret = sistEndret
    }

    private fun filtreringsperiode(): Periode {
        return Periode(this.fom, this.tom)
    }

    fun erFerdigstilt() = tilstand.type == InnsendingTilstandType.InnsendingFerdigstilt

    constructor(
        journalpostId: String,
        ident: String,
        fom: LocalDate,
        tom: LocalDate,
    ) : this(
        id = randomId(),
        ident = ident,
        journalpostId = journalpostId,
        fom = fom,
        tom = tom,
        tilstand = InnsendingRegistrert,
        sistEndret = null,
        personopplysninger = null,
        aktivitetslogg = Aktivitetslogg(),
    )

    companion object {

        fun randomId() = InnsendingId.random()

        fun fromDb(
            id: InnsendingId,
            journalpostId: String,
            ident: String,
            fom: LocalDate,
            tom: LocalDate,
            tilstand: String,
            sistEndret: LocalDateTime,
            personopplysningerliste: List<Personopplysninger>,
            tidsstempelPersonopplysningerInnhentet: LocalDateTime?,
            tidsstempelSkjermingInnhentet: LocalDateTime?,
            aktivitetslogg: Aktivitetslogg,
        ): Innsending {
            return Innsending(
                id = id,
                journalpostId = journalpostId,
                ident = ident,
                fom = fom,
                tom = tom,
                tilstand = convertTilstand(tilstand),
                sistEndret = sistEndret,
                personopplysninger = tidsstempelPersonopplysningerInnhentet?.let {
                    InnhentedePersonopplysninger(
                        personopplysningerliste = personopplysningerliste,
                        tidsstempelInnhentet = it,
                        tidsstempelSkjermingInnhentet = tidsstempelSkjermingInnhentet,
                    )
                },
                aktivitetslogg = aktivitetslogg,
            )
        }

        private fun convertTilstand(tilstand: String): Tilstand {
            return when (InnsendingTilstandType.valueOf(tilstand)) {
                InnsendingTilstandType.InnsendingRegistrert -> InnsendingRegistrert
                InnsendingTilstandType.AvventerPersonopplysninger -> AvventerPersonopplysninger
                InnsendingTilstandType.AvventerSkjermingdata -> AvventerSkjermingdata
                InnsendingTilstandType.AvventerTiltak -> AvventerTiltak
                InnsendingTilstandType.InnsendingFerdigstilt -> InnsendingFerdigstilt
                InnsendingTilstandType.FaktainnhentingFeilet -> FaktainnhentingFeilet
            }
        }
    }

    fun håndter(søknadMottattHendelse: SøknadMottattHendelse) {
        if (journalpostId != søknadMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(søknadMottattHendelse, "Registrert SøknadMottattHendelse")
        if (erAlleFaktaInnhentet()) {
            søknadMottattHendelse.error("journalpostId ${søknadMottattHendelse.journalpostId()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, søknadMottattHendelse)
    }

    fun håndter(personopplysningerMottattHendelse: PersonopplysningerMottattHendelse) {
        if (journalpostId != personopplysningerMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(personopplysningerMottattHendelse, "Registrert PersonopplysningerMottattHendelse")
        tilstand.håndter(this, personopplysningerMottattHendelse)
    }

    fun håndter(skjermingMottattHendelse: SkjermingMottattHendelse) {
        if (journalpostId != skjermingMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(skjermingMottattHendelse, "Registrert SkjermingMottattHendelse")
        tilstand.håndter(this, skjermingMottattHendelse)
    }

    fun håndter(tiltakMottattHendelse: TiltakMottattHendelse) {
        if (journalpostId != tiltakMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(tiltakMottattHendelse, "Registrert TiltakMottattHendelse")
        tilstand.håndter(this, tiltakMottattHendelse)
    }

    fun håndter(resetInnsendingHendelse: ResetInnsendingHendelse) {
        if (journalpostId != resetInnsendingHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(resetInnsendingHendelse, "Registrert ResetInnsendingHendelse")
        if (erAlleFaktaInnhentet()) {
            resetInnsendingHendelse.error("journalpostId ${resetInnsendingHendelse.journalpostId()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, resetInnsendingHendelse)
    }

    fun håndter(feilMottattHendelse: FeilMottattHendelse) {
        if (journalpostId != feilMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(feilMottattHendelse, "Registrert FeilMottattHendelse")
        if (erAlleFaktaInnhentet()) {
            feilMottattHendelse.error("journalpostId ${feilMottattHendelse.journalpostId()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, feilMottattHendelse)
    }

    fun håndter(innsendingUtdatertHendelse: InnsendingUtdatertHendelse) {
        if (journalpostId != innsendingUtdatertHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(innsendingUtdatertHendelse, "Registrert InnsendingUtdatertHendelse")
        if (!erAlleFaktaInnhentet()) {
            LOG.info("journalpostId ${innsendingUtdatertHendelse.journalpostId()} er ikke ferdig behandlet, kan ikke oppdateres")
            return
        }
        tilstand.håndter(this, innsendingUtdatertHendelse)
    }

    private fun kontekst(hendelse: InnsendingHendelse, melding: String) {
        hendelse.setForelderAndAddKontekst(this)
        hendelse.addKontekst(this.tilstand)
        hendelse.info(melding)
    }

    // Gang of four State pattern
    interface Tilstand : KontekstLogable {
        val type: InnsendingTilstandType
        val timeout: Duration

        fun håndter(innsending: Innsending, søknadMottattHendelse: SøknadMottattHendelse) {
            søknadMottattHendelse.warn("Forventet ikke SøknadMottattHendelse i ${type.name}")
        }

        fun håndter(innsending: Innsending, personopplysningerMottattHendelse: PersonopplysningerMottattHendelse) {
            innsending.mottaPersonopplysninger(personopplysningerMottattHendelse)
        }

        fun håndter(innsending: Innsending, skjermingMottattHendelse: SkjermingMottattHendelse) {
            innsending.mottaSkjermingsdata(skjermingMottattHendelse, false)
        }

        fun håndter(innsending: Innsending, tiltakMottattHendelse: TiltakMottattHendelse) {
        }

        fun håndter(innsending: Innsending, resetInnsendingHendelse: ResetInnsendingHendelse) {
            innsending.trengerPersonopplysninger(resetInnsendingHendelse)
            innsending.tilstand(resetInnsendingHendelse, AvventerPersonopplysninger)
            resetInnsendingHendelse.info("Innsending resatt, faktainnhenting begynner på nytt")
        }

        fun håndter(innsending: Innsending, feilMottattHendelse: FeilMottattHendelse) {
            innsending.tilstand(feilMottattHendelse, FaktainnhentingFeilet)
            feilMottattHendelse.info("Mottatt ${feilMottattHendelse.feil()} fra faktainnhenter")
        }

        fun håndter(innsending: Innsending, innsendingUtdatertHendelse: InnsendingUtdatertHendelse) {
            innsendingUtdatertHendelse.warn("Forventet ikke InnsendingUtdatertHendelse i ${type.name}")
        }

        fun leaving(innsending: Innsending, hendelse: InnsendingHendelse) {}
        fun entering(innsending: Innsending, hendelse: InnsendingHendelse) {}

        override fun opprettKontekst(): Kontekst {
            return Kontekst(
                "Tilstand",
                mapOf(
                    "tilstandtype" to type.name,
                ),
            )
        }
    }

    internal object InnsendingRegistrert : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.InnsendingRegistrert
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, søknadMottattHendelse: SøknadMottattHendelse) {
            DomeneMetrikker.søknadMottattCounter().increment()
            innsending.trengerPersonopplysninger(søknadMottattHendelse)
            innsending.tilstand(søknadMottattHendelse, AvventerPersonopplysninger)
        }
    }

    internal object AvventerPersonopplysninger : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerPersonopplysninger
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(
            innsending: Innsending,
            personopplysningerMottattHendelse: PersonopplysningerMottattHendelse,
        ) {
            innsending.mottaPersonopplysninger(personopplysningerMottattHendelse)
            innsending.trengerSkjermingdata(personopplysningerMottattHendelse)
            innsending.tilstand(personopplysningerMottattHendelse, AvventerSkjermingdata)
        }
    }

    internal object AvventerSkjermingdata : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerSkjermingdata
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, skjermingMottattHendelse: SkjermingMottattHendelse) {
            innsending.mottaSkjermingsdata(skjermingMottattHendelse, true)
            innsending.trengerTiltak(skjermingMottattHendelse)
            innsending.tilstand(skjermingMottattHendelse, AvventerTiltak)
        }
    }

    internal object AvventerTiltak : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerTiltak
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, tiltakMottattHendelse: TiltakMottattHendelse) {
            tiltakMottattHendelse.info("Fikk info om tiltak: ${tiltakMottattHendelse.tiltaksaktivitet()}")
            innsending.tilstand(tiltakMottattHendelse, InnsendingFerdigstilt)
        }
    }

    internal object InnsendingFerdigstilt : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.InnsendingFerdigstilt
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, innsendingUtdatertHendelse: InnsendingUtdatertHendelse) {
            innsendingUtdatertHendelse.info(
                "Mottatt InnsendingUtdatertHendelse, trenger å oppdatere faktaene vi har hentet inn",
            )
            innsending.trengerPersonopplysninger(innsendingUtdatertHendelse)
            innsending.trengerSkjermingdata(innsendingUtdatertHendelse)
            innsending.trengerTiltak(innsendingUtdatertHendelse)
        }
    }

    internal object FaktainnhentingFeilet : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.FaktainnhentingFeilet
        override val timeout: Duration
            get() = Duration.ofDays(1)
    }

    private fun trengerPersonopplysninger(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.personopplysninger,
            melding = "Trenger personopplysninger",
            detaljer = mapOf("ident" to this.ident),
        )
    }

    private fun trengerSkjermingdata(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.skjerming,
            melding = "Trenger skjermingdata",
            detaljer = mapOf(
                "ident" to this.ident,
                "barn" to this.personopplysningerBarnMedIdent().map { it.ident },
            ),
        )
    }

    private fun trengerTiltak(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.tiltak,
            melding = "Trenger tiltak",
            detaljer = mapOf("ident" to this.ident),
        )
    }

    private fun tilstand(
        event: InnsendingHendelse,
        nyTilstand: Tilstand,
        block: () -> Unit = {},
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
        gjeldendeTilstand: InnsendingTilstandType,
        aktivitetslogg: Aktivitetslogg,
        forrigeTilstand: InnsendingTilstandType,
        timeout: Duration,
    ) {
        observers.forEach {
            it.tilstandEndret(
                InnsendingObserver.InnendingEndretTilstandEvent(
                    journalpostId = journalpostId,
                    gjeldendeTilstand = gjeldendeTilstand,
                    forrigeTilstand = forrigeTilstand,
                    aktivitetslogg = aktivitetslogg,
                    timeout = timeout,
                ),
            )
        }
    }

    fun addObserver(observer: InnsendingObserver) {
        observers.add(observer)
    }

    private fun erAlleFaktaInnhentet() =
        this.tilstand.type in setOf(
            InnsendingTilstandType.InnsendingFerdigstilt,
        )

    override fun opprettKontekst(): Kontekst =
        Kontekst(
            "Innsending",
            mapOf("journalpostId" to journalpostId),
        )

    fun sjekkOmSaksbehandlerHarTilgang(saksbehandler: Saksbehandler) {
        fun sjekkBeskyttelsesbehovStrengtFortrolig(harBeskyttelsesbehovStrengtFortrolig: Boolean) {
            if (harBeskyttelsesbehovStrengtFortrolig) {
                SECURELOG.info("erStrengtFortrolig")
                // Merk at vi ikke sjekker egenAnsatt her, strengt fortrolig trumfer det
                if (Rolle.STRENGT_FORTROLIG_ADRESSE in saksbehandler.roller) {
                    SECURELOG.info("Access granted to strengt fortrolig for $ident")
                } else {
                    SECURELOG.info("Access denied to strengt fortrolig for $ident")
                    throw TilgangException("Saksbehandler har ikke tilgang")
                }
            }
        }

        fun sjekkBeskytelsesbehovFortrolig(harBeskyttelsesbehovFortrolig: Boolean) {
            if (harBeskyttelsesbehovFortrolig) {
                SECURELOG.info("erFortrolig")
                // Merk at vi ikke sjekker egenAnsatt her, fortrolig trumfer det
                if (Rolle.FORTROLIG_ADRESSE in saksbehandler.roller) {
                    SECURELOG.info("Access granted to fortrolig for $ident")
                } else {
                    SECURELOG.info("Access denied to fortrolig for $ident")
                    throw TilgangException("Saksbehandler har ikke tilgang")
                }
            }
        }

        fun sjekkBeskyttelsesbehovSkjermet(
            erEgenAnsatt: Boolean,
            harBeskyttelsesbehovFortrolig: Boolean,
            harBeskyttelsesbehovStrengtFortrolig: Boolean,
        ) {
            if (erEgenAnsatt && !(harBeskyttelsesbehovFortrolig || harBeskyttelsesbehovStrengtFortrolig)) {
                SECURELOG.info("erEgenAnsatt")
                // Er kun egenAnsatt, har ikke et beskyttelsesbehov i tillegg
                if (Rolle.SKJERMING in saksbehandler.roller) {
                    SECURELOG.info("Access granted to egen ansatt for $ident")
                } else {
                    SECURELOG.info("Access denied to egen ansatt for $ident")
                    throw TilgangException("Saksbehandler har ikke tilgang")
                }
            }
        }

        fun sjekkSøkerForTilgang(personopplysninger: PersonopplysningerSøker) {
            val harBeskyttelsesbehovFortrolig = personopplysninger.fortrolig
            val harBeskyttelsesbehovStrengtFortrolig =
                personopplysninger.strengtFortrolig || personopplysninger.strengtFortroligUtland
            val erEgenAnsatt = personopplysninger.skjermet ?: false

            sjekkBeskyttelsesbehovStrengtFortrolig(harBeskyttelsesbehovStrengtFortrolig)
            sjekkBeskytelsesbehovFortrolig(harBeskyttelsesbehovFortrolig)
            sjekkBeskyttelsesbehovSkjermet(
                erEgenAnsatt,
                harBeskyttelsesbehovFortrolig,
                harBeskyttelsesbehovStrengtFortrolig,
            )
        }

        fun sjekkBarnMedIdentForTilgang(personopplysninger: PersonopplysningerBarnMedIdent) {
            val harBeskyttelsesbehovFortrolig = personopplysninger.fortrolig
            val harBeskyttelsesbehovStrengtFortrolig =
                personopplysninger.strengtFortrolig || personopplysninger.strengtFortroligUtland

            sjekkBeskyttelsesbehovStrengtFortrolig(harBeskyttelsesbehovStrengtFortrolig)
            sjekkBeskytelsesbehovFortrolig(harBeskyttelsesbehovFortrolig)
        }

        personopplysningerSøker()?.let { sjekkSøkerForTilgang(it) }
            ?: throw TilgangException("Umulig å vurdere tilgang")
        personopplysningerBarnMedIdent().forEach {
            sjekkBarnMedIdentForTilgang(it)
        }
    }

    private fun mottaPersonopplysninger(
        personopplysningerMottattHendelse: PersonopplysningerMottattHendelse,
    ) {
        if (this.personopplysninger != null &&
            !personopplysningerMottattHendelse.tidsstempelPersonopplysningerInnhentet()
                .isAfter(this.personopplysninger!!.tidsstempelInnhentet)
        ) {
            personopplysningerMottattHendelse
                .info("Fikk utdatert info om person saker, lagrer ikke")
            return
        }

        personopplysningerMottattHendelse
            .info("Fikk info om person saker: ${personopplysningerMottattHendelse.personopplysninger()}")
        this.personopplysninger = InnhentedePersonopplysninger(
            tidsstempelInnhentet = personopplysningerMottattHendelse.tidsstempelPersonopplysningerInnhentet(),
            personopplysningerliste = personopplysningerMottattHendelse.personopplysninger(),
        )
    }

    private fun mottaSkjermingsdata(
        skjermingMottattHendelse: SkjermingMottattHendelse,
        exceptionHvisManglendePersonopplysninger: Boolean,
    ) {
        skjermingMottattHendelse.info("Fikk info om skjerming: ${skjermingMottattHendelse.skjerming()}")
        if (this.personopplysninger == null) {
            if (exceptionHvisManglendePersonopplysninger) {
                skjermingMottattHendelse.severe("Skjerming kan ikke settes når vi ikke har noe Personopplysninger")
            } else {
                skjermingMottattHendelse.warn("Skjerming kan ikke settes når vi ikke har noe Personopplysninger")
            }
            return
        }

        if (this.personopplysninger!!.tidsstempelSkjermingInnhentet != null &&
            !skjermingMottattHendelse.tidsstempelSkjermingInnhentet()
                .isAfter(this.personopplysninger!!.tidsstempelSkjermingInnhentet)
        ) {
            skjermingMottattHendelse.info("Fikk utdatert info om skjerming, lagrer ikke")
            return
        }

        this.personopplysninger = InnhentedePersonopplysninger(
            tidsstempelInnhentet = this.personopplysninger!!.tidsstempelInnhentet,
            tidsstempelSkjermingInnhentet = skjermingMottattHendelse.tidsstempelSkjermingInnhentet(),
            personopplysningerliste = this.personopplysninger!!.personopplysningerliste.map {
                when (it) {
                    is PersonopplysningerBarnMedIdent -> it.copy(
                        skjermet = skjermingMottattHendelse.skjerming()
                            .barn.firstOrNull { barn -> barn.ident == it.ident }?.skjerming,
                    )

                    is PersonopplysningerBarnUtenIdent -> it
                    is PersonopplysningerSøker -> it.copy(
                        skjermet = skjermingMottattHendelse.skjerming().søker.skjerming,
                    )
                }
            },
        )
    }

    class DirtyChecker {
        val properties: MutableMap<String, AtomicBoolean> = mutableMapOf()

        fun get(name: String): AtomicBoolean = properties.getOrPut(name) { AtomicBoolean(false) }
        fun set(name: String) = get(name).set(true)
        fun isDirty(): Boolean = properties.any { it.value.get() }
    }
}
