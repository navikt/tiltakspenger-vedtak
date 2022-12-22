package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.felles.DomeneMetrikker
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.helper.DirtyCheckingAktivitetslogg
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.ResetInnsendingHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

@Suppress("TooManyFunctions", "LongParameterList")
class Innsending private constructor(
    val id: InnsendingId,
    val journalpostId: String,
    val ident: String,
    tilstand: Tilstand,
    søknad: Søknad?,
    sistEndret: LocalDateTime?,
    personopplysninger: List<Personopplysninger>,
    tiltak: List<Tiltaksaktivitet>,
    ytelser: List<YtelseSak>,
    aktivitetslogg: Aktivitetslogg
) : KontekstLogable {
    private val dirtyChecker: DirtyChecker = DirtyChecker()

    val aktivitetslogg: IAktivitetslogg =
        DirtyCheckingAktivitetslogg(aktivitetslogg, dirtyChecker.get("aktivitetslogg"))

    fun isDirty() = dirtyChecker.isDirty()

    var tilstand: Tilstand = tilstand
        private set(value) {
            field = value
            dirtyChecker.set("tilstand")
        }

    var søknad: Søknad? = søknad
        private set(value) {
            field = value
            dirtyChecker.set("søknad")
        }
    var personopplysninger: List<Personopplysninger> = personopplysninger
        private set(value) {
            field = value
            dirtyChecker.set("personopplysninger")
        }
    var tiltak: List<Tiltaksaktivitet> = tiltak
        private set(value) {
            field = value
            dirtyChecker.set("tiltak")
        }
    var ytelser: List<YtelseSak> = ytelser
        private set(value) {
            field = value
            dirtyChecker.set("ytelser")
        }
    var sistEndret: LocalDateTime? = sistEndret
        private set

    private val observers = mutableSetOf<InnsendingObserver>()

    fun personopplysningerSøker() = personopplysninger.filterIsInstance<Personopplysninger.Søker>().firstOrNull()
    fun personopplysningerBarnUtenIdent() = personopplysninger.filterIsInstance<Personopplysninger.BarnUtenIdent>()
    fun personopplysningerBarnMedIdent() = personopplysninger.filterIsInstance<Personopplysninger.BarnMedIdent>()

    fun arenaTiltaksaktivitetForSøknad(søknad: Søknad): Tiltaksaktivitet? =
        if (søknad.tiltak is Tiltak.ArenaTiltak) {
            this.tiltak.firstOrNull { it.aktivitetId == søknad.tiltak.arenaId }
        } else null

    private fun finnFomOgTom(søknad: Søknad): Pair<LocalDate, LocalDate?> {
        fun tidligsteDato(dato: LocalDate, vararg datoer: LocalDate?): LocalDate =
            (datoer.toList() + dato).filterNotNull().min()

        fun senesteDato(dato: LocalDate, vararg datoer: LocalDate?): LocalDate =
            (datoer.toList() + dato).filterNotNull().max()

        fun senesteDato(vararg datoer: LocalDate?): LocalDate? = datoer.filterNotNull().maxOrNull()

        val søknadFom: LocalDate = søknad.tiltak?.startdato ?: LocalDate.EPOCH
        val søknadTom: LocalDate? = søknad.tiltak?.sluttdato

        val tiltakFom: LocalDate? = arenaTiltaksaktivitetForSøknad(søknad)?.deltakelsePeriode?.fom
        val tiltakTom: LocalDate? = arenaTiltaksaktivitetForSøknad(søknad)?.deltakelsePeriode?.tom

        val tidligsteFom: LocalDate = tidligsteDato(søknadFom, tiltakFom)

        val senesteTom: LocalDate? = senesteDato(søknadTom, tiltakTom)
        return Pair(tidligsteFom, senesteTom)
    }

    fun oppdaterSistEndret(sistEndret: LocalDateTime) {
        this.sistEndret = sistEndret
    }

    fun vurderingsperiodeForSøknad(): Periode? {
        return this.søknad?.let {
            val (tidligsteFom: LocalDate, senesteTom: LocalDate?) = finnFomOgTom(it)
            senesteTom?.let { Periode(tidligsteFom, senesteTom) }
        }
    }

    fun filtreringsperiode(): Periode {
        return søknad?.let {
            val (tidligsteFom: LocalDate, senesteTom: LocalDate?) = finnFomOgTom(it)
            Periode(tidligsteFom, senesteTom ?: LocalDate.MAX)
        } ?: Periode(LocalDate.MIN, LocalDate.MAX)
    }

    constructor(
        journalpostId: String,
        ident: String,
    ) : this(
        id = randomId(),
        ident = ident,
        journalpostId = journalpostId,
        tilstand = InnsendingRegistrert,
        søknad = null,
        sistEndret = null,
        personopplysninger = mutableListOf(),
        tiltak = mutableListOf(),
        ytelser = mutableListOf(),
        aktivitetslogg = Aktivitetslogg()
    )

    companion object {

        fun randomId() = InnsendingId.random()

        fun fromDb(
            id: InnsendingId,
            journalpostId: String,
            ident: String,
            tilstand: String,
            søknad: Søknad?,
            sistEndret: LocalDateTime,
            tiltak: List<Tiltaksaktivitet>,
            ytelser: List<YtelseSak>,
            personopplysninger: List<Personopplysninger>,
            aktivitetslogg: Aktivitetslogg,
        ): Innsending {
            return Innsending(
                id = id,
                journalpostId = journalpostId,
                ident = ident,
                tilstand = convertTilstand(tilstand),
                søknad = søknad,
                sistEndret = sistEndret,
                personopplysninger = personopplysninger,
                tiltak = tiltak,
                ytelser = ytelser,
                aktivitetslogg = aktivitetslogg,
            )
        }

        private fun convertTilstand(tilstand: String): Tilstand {
            return when (InnsendingTilstandType.valueOf(tilstand)) {
                InnsendingTilstandType.InnsendingRegistrert -> InnsendingRegistrert
                InnsendingTilstandType.AvventerPersonopplysninger -> AvventerPersonopplysninger
                InnsendingTilstandType.AvventerSkjermingdata -> AvventerSkjermingdata
                InnsendingTilstandType.AvventerTiltak -> AvventerTiltak
                InnsendingTilstandType.AvventerYtelser -> AvventerYtelser
                InnsendingTilstandType.InnsendingFerdigstilt -> SøkerFerdigstiltType
                InnsendingTilstandType.FaktainnhentingFeilet -> FaktainnhentingFeilet
                else -> throw IllegalStateException("Ukjent tilstand $tilstand")
            }
        }
    }

    fun håndter(søknadMottattHendelse: SøknadMottattHendelse) {
        if (journalpostId != søknadMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(søknadMottattHendelse, "Registrert SøknadMottattHendelse")
        if (erFerdigBehandlet()) {
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
        if (erFerdigBehandlet()) {
            personopplysningerMottattHendelse
                .error("journalpostId ${personopplysningerMottattHendelse.journalpostId()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, personopplysningerMottattHendelse)
    }

    fun håndter(skjermingMottattHendelse: SkjermingMottattHendelse) {
        if (journalpostId != skjermingMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(skjermingMottattHendelse, "Registrert SkjermingMottattHendelse")
        if (erFerdigBehandlet()) {
            skjermingMottattHendelse.error("journalpostId ${skjermingMottattHendelse.journalpostId()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, skjermingMottattHendelse)
    }

    fun håndter(arenaTiltakMottattHendelse: ArenaTiltakMottattHendelse) {
        if (journalpostId != arenaTiltakMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(arenaTiltakMottattHendelse, "Registrert ArenaTiltakMottattHendelse")
        if (erFerdigBehandlet()) {
            LOG.info("${arenaTiltakMottattHendelse.journalpostId()} er allerede ferdig behandlet")
            arenaTiltakMottattHendelse.error("journalpostId ${arenaTiltakMottattHendelse.journalpostId()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, arenaTiltakMottattHendelse)
    }

    fun håndter(ytelserMottattHendelse: YtelserMottattHendelse) {
        if (journalpostId != ytelserMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(ytelserMottattHendelse, "Registrert YtelserMottattHendelse")
        if (erFerdigBehandlet()) {
            ytelserMottattHendelse.error("journalpostId ${ytelserMottattHendelse.journalpostId()} allerede ferdig behandlet")
            return
        }
        tilstand.håndter(this, ytelserMottattHendelse)
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
            personopplysningerMottattHendelse.warn("Forventet ikke PersonopplysningerMottattHendelse i ${type.name}")
        }

        fun håndter(innsending: Innsending, skjermingMottattHendelse: SkjermingMottattHendelse) {
            skjermingMottattHendelse.warn("Forventet ikke SkjermingMottattHendelse i ${type.name}")
        }

        fun håndter(innsending: Innsending, arenaTiltakMottattHendelse: ArenaTiltakMottattHendelse) {
            arenaTiltakMottattHendelse.warn("Forventet ikke ArenaTiltakMottattHendelse i ${type.name}")
        }

        fun håndter(innsending: Innsending, ytelserMottattHendelse: YtelserMottattHendelse) {
            ytelserMottattHendelse.warn("Forventet ikke YtelserMottattHendelse i ${type.name}")
        }

        fun håndter(innsending: Innsending, resetInnsendingHendelse: ResetInnsendingHendelse) {
            innsending.trengerPersonopplysninger(resetInnsendingHendelse)
            innsending.tilstand(resetInnsendingHendelse, AvventerPersonopplysninger)
            resetInnsendingHendelse.info("Innsending resatt, faktainnhenting begynner på nytt")
        }

        fun leaving(innsending: Innsending, hendelse: InnsendingHendelse) {}
        fun entering(innsending: Innsending, hendelse: InnsendingHendelse) {}

        override fun opprettKontekst(): Kontekst {
            return Kontekst(
                "Tilstand",
                mapOf(
                    "tilstandtype" to type.name
                )
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
            søknadMottattHendelse.søknad().tiltak?.tiltakskode?.let {
                if (it.tiltaksgruppe == Tiltaksaktivitet.Tiltaksgruppe.UTFAS) {
                    DomeneMetrikker.utfasCounter(it)
                }
            }
            innsending.søknad = søknadMottattHendelse.søknad()
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
            personopplysningerMottattHendelse: PersonopplysningerMottattHendelse
        ) {
            personopplysningerMottattHendelse
                .info("Fikk info om person saker: ${personopplysningerMottattHendelse.personopplysninger()}")
            innsending.personopplysninger = personopplysningerMottattHendelse.personopplysninger()
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
            skjermingMottattHendelse.info("Fikk info om skjerming: ${skjermingMottattHendelse.skjerming()}")
            if (innsending.personopplysningerSøker() == null) {
                skjermingMottattHendelse.severe("Skjerming kan ikke settes når vi ikke har noe Personopplysninger")
            }
            innsending.personopplysninger = innsending.personopplysninger.map {
                if (it is Personopplysninger.Søker) {
                    it.copy(
                        skjermet = skjermingMottattHendelse.skjerming().skjerming
                    )
                } else {
                    it
                }
            }
            innsending.trengerTiltak(skjermingMottattHendelse)
            innsending.tilstand(skjermingMottattHendelse, AvventerTiltak)
        }
    }

    internal object AvventerTiltak : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerTiltak
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, arenaTiltakMottattHendelse: ArenaTiltakMottattHendelse) {

            fun earliest(fom: LocalDate?, tom: LocalDate?) =
                when {
                    fom != null && tom != null -> if (tom.isBefore(fom)) {
                        LOG.warn { "fom er etter tom, så vi bytter om de to datoene på tiltaket" }
                        tom
                    } else {
                        fom
                    }

                    else -> fom ?: LocalDate.MIN
                }


            fun latest(fom: LocalDate?, tom: LocalDate?) =
                when {
                    fom != null && tom != null -> if (fom.isAfter(tom)) fom else tom
                    else -> tom ?: LocalDate.MAX
                }

            when (arenaTiltakMottattHendelse.feilmelding()) {
                ArenaTiltakMottattHendelse.Feilmelding.PersonIkkeFunnet -> {
                    arenaTiltakMottattHendelse.error("Fant ikke person i arenetiltak")
                    innsending.tilstand(arenaTiltakMottattHendelse, FaktainnhentingFeilet)
                }

                null -> {
                    arenaTiltakMottattHendelse
                        .info("Fikk info om arenaTiltak: ${arenaTiltakMottattHendelse.tiltaksaktivitet()}")
                    innsending.tiltak = arenaTiltakMottattHendelse.tiltaksaktivitet()!!.filter {
                        LOG.info { "filtreringsperiode : ${innsending.filtreringsperiode()}" }
                        LOG.info { "deltakelsePeriode.fom : ${it.deltakelsePeriode.fom}" }
                        LOG.info { "deltakelsePeriode.tom : ${it.deltakelsePeriode.tom}" }
                        val periode = Periode(
                            earliest(it.deltakelsePeriode.fom, it.deltakelsePeriode.tom),
                            latest(it.deltakelsePeriode.fom, it.deltakelsePeriode.tom)
                        )
                        LOG.info { "periode : $periode" }
                        innsending.filtreringsperiode().overlapperMed(periode)
                    }.also {
                        val antall = arenaTiltakMottattHendelse.tiltaksaktivitet()!!.size - innsending.tiltak.size
                        LOG.info { "Filtrerte bort $antall gamle tiltak" }
                    }
                    innsending.trengerArenaYtelse(arenaTiltakMottattHendelse)
                    innsending.tilstand(arenaTiltakMottattHendelse, AvventerYtelser)
                }
            }
        }
    }

    internal object AvventerYtelser : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerYtelser
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, ytelserMottattHendelse: YtelserMottattHendelse) {
            ytelserMottattHendelse.info("Fikk info om arenaYtelser: ${ytelserMottattHendelse.ytelseSak()}")
            innsending.ytelser = ytelserMottattHendelse.ytelseSak().filter {
                val tomDato = it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX
                innsending.filtreringsperiode().overlapperMed(
                    Periode(
                        it.fomGyldighetsperiode.toLocalDate(),
                        tomDato.let { tom ->
                            if (tom < it.fomGyldighetsperiode.toLocalDate()) LocalDate.MAX else tom
                        }
                    )
                )
            }.also {
                val antall = ytelserMottattHendelse.ytelseSak().size - innsending.ytelser.size
                LOG.info { "Filtrerte bort $antall gamle ytelser" }
            }
            innsending.tilstand(ytelserMottattHendelse, SøkerFerdigstiltType)
        }
    }

    internal object SøkerFerdigstiltType : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.InnsendingFerdigstilt
        override val timeout: Duration
            get() = Duration.ofDays(1)
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
            detaljer = mapOf("ident" to this.ident)
        )
    }

    private fun trengerSkjermingdata(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.skjerming,
            melding = "Trenger skjermingdata",
            detaljer = mapOf("ident" to this.ident)
        )
    }

    private fun trengerTiltak(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.arenatiltak,
            melding = "Trenger arenatiltak",
            detaljer = mapOf("ident" to this.ident)
        )
    }

    private fun trengerArenaYtelse(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.arenaytelser,
            melding = "Trenger arenaytelser",
            detaljer = mapOf("ident" to this.ident)
        )
    }

    private fun tilstand(
        event: InnsendingHendelse,
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
        gjeldendeTilstand: InnsendingTilstandType,
        aktivitetslogg: Aktivitetslogg,
        forrigeTilstand: InnsendingTilstandType,
        timeout: Duration
    ) {
        observers.forEach {
            it.tilstandEndret(
                InnsendingObserver.InnendingEndretTilstandEvent(
                    journalpostId = journalpostId,
                    gjeldendeTilstand = gjeldendeTilstand,
                    forrigeTilstand = forrigeTilstand,
                    aktivitetslogg = aktivitetslogg,
                    timeout = timeout
                )
            )
        }
    }

    fun addObserver(observer: InnsendingObserver) {
        observers.add(observer)
    }

    private fun erFerdigBehandlet() =
        this.tilstand.type in setOf(
            InnsendingTilstandType.InnsendingFerdigstilt,
            InnsendingTilstandType.AlleredeBehandlet
        )

    override fun opprettKontekst(): Kontekst = Kontekst(
        "Innsending",
        mapOf("journalpostId" to journalpostId)
    )

    fun sjekkOmSaksbehandlerHarTilgang(saksbehandler: Saksbehandler) {

        fun sjekkBeskyttelsesbehovStrengtFortrolig(harBeskyttelsesbehovStrengtFortrolig: Boolean) {
            if (harBeskyttelsesbehovStrengtFortrolig) {
                SECURELOG.info("erStrengtFortrolig")
                //Merk at vi ikke sjekker egenAnsatt her, strengt fortrolig trumfer det
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
                //Merk at vi ikke sjekker egenAnsatt her, fortrolig trumfer det
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
            harBeskyttelsesbehovStrengtFortrolig: Boolean
        ) {
            if (erEgenAnsatt && !(harBeskyttelsesbehovFortrolig || harBeskyttelsesbehovStrengtFortrolig)) {
                SECURELOG.info("erEgenAnsatt")
                //Er kun egenAnsatt, har ikke et beskyttelsesbehov i tillegg
                if (Rolle.SKJERMING in saksbehandler.roller) {
                    SECURELOG.info("Access granted to egen ansatt for $ident")
                } else {
                    SECURELOG.info("Access denied to egen ansatt for $ident")
                    throw TilgangException("Saksbehandler har ikke tilgang")
                }
            }
        }

        fun sjekkSøkerForTilgang(personopplysninger: Personopplysninger.Søker) {
            val harBeskyttelsesbehovFortrolig = personopplysninger.fortrolig
            val harBeskyttelsesbehovStrengtFortrolig =
                personopplysninger.strengtFortrolig || personopplysninger.strengtFortroligUtland
            val erEgenAnsatt = personopplysninger.skjermet ?: false

            sjekkBeskyttelsesbehovStrengtFortrolig(harBeskyttelsesbehovStrengtFortrolig)
            sjekkBeskytelsesbehovFortrolig(harBeskyttelsesbehovFortrolig)
            sjekkBeskyttelsesbehovSkjermet(
                erEgenAnsatt,
                harBeskyttelsesbehovFortrolig,
                harBeskyttelsesbehovStrengtFortrolig
            )
        }

        fun sjekkBarnMedIdentForTilgang(personopplysninger: Personopplysninger.BarnMedIdent) {
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


    // Jeg har fjernet flere av
    // private fun emit* funksjonene

    class DirtyChecker {
        val properties: MutableMap<String, AtomicBoolean> = mutableMapOf()

        fun get(name: String): AtomicBoolean = properties.getOrPut(name) { AtomicBoolean(false) }
        fun set(name: String) = get(name).set(true)
        fun isDirty(): Boolean = properties.any { it.value.get() }
    }
}
