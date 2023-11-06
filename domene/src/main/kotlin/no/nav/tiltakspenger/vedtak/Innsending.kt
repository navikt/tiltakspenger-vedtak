package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.felles.DomeneMetrikker
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.desember
import no.nav.tiltakspenger.vedtak.helper.DirtyCheckingAktivitetslogg
import no.nav.tiltakspenger.vedtak.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.ForeldrepengerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.InnsendingUtdatertHendelse
import no.nav.tiltakspenger.vedtak.meldinger.OvergangsstønadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.ResetInnsendingHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.TiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.UføreMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse
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
    tilstand: Tilstand,
    søknad: Søknad?,
    sistEndret: LocalDateTime?,
    personopplysninger: InnhentedePersonopplysninger?,
//    tiltak: InnhentedeTiltak?,
    ytelser: InnhentedeArenaYtelser?,
    foreldrepengerVedtak: InnhentedeForeldrepengerVedtak?,
    overgangsstønadVedtak: InnhentedeOvergangsstønadVedtak?,
    uføreVedtak: InnhentetUføre?,
    aktivitetslogg: Aktivitetslogg,
) : KontekstLogable {
    private val dirtyChecker: DirtyChecker = DirtyChecker()

    val aktivitetslogg: IAktivitetslogg =
        DirtyCheckingAktivitetslogg(aktivitetslogg, dirtyChecker.get("aktivitetslogg"))

    fun isDirty() = dirtyChecker.isDirty()

    fun endringsHash(): String =
        listOfNotNull(
            søknad?.tidsstempelHosOss,
            personopplysninger?.tidsstempelInnhentet,
//            tiltak?.tidsstempelInnhentet,
            ytelser?.tidsstempelInnhentet,
            foreldrepengerVedtak?.tidsstempelInnhentet,
            uføreVedtak?.tidsstempelInnhentet,
            overgangsstønadVedtak?.tidsstempelInnhentet,
        ).fold(journalpostId.hashCode()) { hash, tidsstempel -> 31 * hash + tidsstempel.hashCode() }.toString()

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

    var personopplysninger: InnhentedePersonopplysninger? = personopplysninger
        private set(value) {
            field = value
            dirtyChecker.set("personopplysninger")
        }

//    var tiltak: InnhentedeTiltak? = tiltak
//        set(value) {
//            field = value
//            dirtyChecker.set("tiltak")
//        }

    var ytelser: InnhentedeArenaYtelser? = ytelser
        private set(value) {
            field = value
            dirtyChecker.set("ytelser")
        }

    var foreldrepengerVedtak: InnhentedeForeldrepengerVedtak? = foreldrepengerVedtak
        private set(value) {
            field = value
            dirtyChecker.set("foreldrepenger")
        }

    var overgangsstønadVedtak: InnhentedeOvergangsstønadVedtak? = overgangsstønadVedtak
        private set(value) {
            field = value
            dirtyChecker.set("overgangsstønad")
        }

    var uføreVedtak: InnhentetUføre? = uføreVedtak
        private set(value) {
            field = value
            dirtyChecker.set("uføre")
        }

    var sistEndret: LocalDateTime? = sistEndret
        private set

    private val observers = mutableSetOf<InnsendingObserver>()

    fun personopplysningerSøker() =
        personopplysninger?.personopplysningerliste?.filterIsInstance<Personopplysninger.Søker>()?.firstOrNull()

    fun personopplysningerBarnUtenIdent() =
        personopplysninger?.personopplysningerliste?.filterIsInstance<Personopplysninger.BarnUtenIdent>() ?: emptyList()

    private fun personopplysningerBarnMedIdent() =
        personopplysninger?.personopplysningerliste?.filterIsInstance<Personopplysninger.BarnMedIdent>() ?: emptyList()

//    fun tiltakForSøknad(søknad: Søknad): Tiltak? =
//        if (søknad.tiltak is SøknadsTiltak.ArenaTiltak) {
//            this.tiltak?.tiltaksliste?.firstOrNull { it.id == søknad.tiltak.arenaId } // TODO: Denne vil aldri slå til, man sammenligner epler og pærer
//        } else {
//            null
//        }

    private fun finnFomOgTom(søknad: Søknad): Pair<LocalDate?, LocalDate?> {
        fun tidligsteDato(dato: LocalDate?, vararg datoer: LocalDate?): LocalDate? =
            (datoer.toList() + dato).filterNotNull().minOrNull()

        fun senesteDato(dato: LocalDate, vararg datoer: LocalDate?): LocalDate =
            (datoer.toList() + dato).filterNotNull().max()

        fun senesteDato(vararg datoer: LocalDate?): LocalDate? = datoer.filterNotNull().maxOrNull()

        val søknadFom: LocalDate? = søknad.tiltak?.startdato
        val søknadTom: LocalDate? = søknad.tiltak?.sluttdato

//        val tiltakFom: LocalDate? = tiltakForSøknad(søknad)?.deltakelseFom
//        val tiltakTom: LocalDate? = tiltakForSøknad(søknad)?.deltakelseTom

//        val tidligsteFom: LocalDate? = tidligsteDato(søknadFom, tiltakFom)
//
//        val senesteTom: LocalDate? = senesteDato(søknadTom, tiltakTom)
//        return Pair(tidligsteFom, senesteTom)
        return Pair(søknadFom, søknadTom)
    }

    fun oppdaterSistEndret(sistEndret: LocalDateTime) {
        this.sistEndret = sistEndret
    }

    fun vurderingsperiodeForSøknad(): Periode? {
        return this.søknad?.let {
            val (tidligsteFom: LocalDate?, senesteTom: LocalDate?) = finnFomOgTom(it)
            if (tidligsteFom != null && senesteTom != null) Periode(tidligsteFom, senesteTom) else null
        }
    }

    // TODO: MIN eller EPOCH ? MAX eller LocalDate.of(9999,12,31)
    private fun filtreringsperiode(): Periode {
        return søknad?.let {
            val (tidligsteFom: LocalDate?, senesteTom: LocalDate?) = finnFomOgTom(it)
            Periode(
                tidligsteFom ?: LocalDate.EPOCH,
                senesteTom ?: 31.desember(9999),
            )
        } ?: Periode(LocalDate.EPOCH, 31.desember(9999))
    }

    fun erFerdigstilt() = tilstand.type == InnsendingTilstandType.InnsendingFerdigstilt

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
        personopplysninger = null,
//        tiltak = null,
        ytelser = null,
        foreldrepengerVedtak = null,
        overgangsstønadVedtak = null,
        uføreVedtak = null,
        aktivitetslogg = Aktivitetslogg(),
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
            foreldrepengerVedtak: List<ForeldrepengerVedtak>,
            overgangsstønadVedtak: List<OvergangsstønadVedtak>,
            uføreVedtak: UføreVedtak?,
            personopplysningerliste: List<Personopplysninger>,
//            tiltaksliste: List<Tiltaksaktivitet>,
            ytelserliste: List<YtelseSak>,
//            tidsstempelTiltakInnhentet: LocalDateTime?,
            tidsstempelYtelserInnhentet: LocalDateTime?,
            tidsstempelPersonopplysningerInnhentet: LocalDateTime?,
            tidsstempelForeldrepengerVedtakInnhentet: LocalDateTime?,
            tidsstempelOvergangsstønadVedtakInnhentet: LocalDateTime?,
            tidsstempelUføreInnhentet: LocalDateTime?,
            tidsstempelSkjermingInnhentet: LocalDateTime?,
            aktivitetslogg: Aktivitetslogg,
        ): Innsending {
            return Innsending(
                id = id,
                journalpostId = journalpostId,
                ident = ident,
                tilstand = convertTilstand(tilstand),
                søknad = søknad,
                sistEndret = sistEndret,
                personopplysninger = tidsstempelPersonopplysningerInnhentet?.let {
                    InnhentedePersonopplysninger(
                        personopplysningerliste = personopplysningerliste,
                        tidsstempelInnhentet = it,
                        tidsstempelSkjermingInnhentet = tidsstempelSkjermingInnhentet,
                    )
                },
//                tiltak = tidsstempelTiltakInnhentet?.let { InnhentedeTiltak(tiltaksliste, it) },
                ytelser = tidsstempelYtelserInnhentet?.let { InnhentedeArenaYtelser(ytelserliste, it) },
                foreldrepengerVedtak = tidsstempelForeldrepengerVedtakInnhentet?.let {
                    InnhentedeForeldrepengerVedtak(
                        foreldrepengerVedtak,
                        it,
                    )
                },
                overgangsstønadVedtak = tidsstempelOvergangsstønadVedtakInnhentet?.let {
                    InnhentedeOvergangsstønadVedtak(
                        overgangsstønadVedtak,
                        it,
                    )
                },
                uføreVedtak = tidsstempelUføreInnhentet?.let {
                    InnhentetUføre(
                        uføreVedtak,
                        tidsstempelUføreInnhentet,
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
                InnsendingTilstandType.AvventerYtelser -> AvventerYtelser
                InnsendingTilstandType.AvventerForeldrepenger -> AvventerForeldrepenger
                InnsendingTilstandType.AvventerOvergangsstønad -> AvventerOvergangsstønad
                InnsendingTilstandType.AvventerUføre -> AvventerUføre
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
        kontekst(tiltakMottattHendelse, "Registrert ArenaTiltakMottattHendelse")
        tilstand.håndter(this, tiltakMottattHendelse)
    }

    fun håndter(ytelserMottattHendelse: YtelserMottattHendelse) {
        if (journalpostId != ytelserMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(ytelserMottattHendelse, "Registrert YtelserMottattHendelse")
        tilstand.håndter(this, ytelserMottattHendelse)
    }

    fun håndter(foreldrepengerMottattHendelse: ForeldrepengerMottattHendelse) {
        if (journalpostId != foreldrepengerMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(foreldrepengerMottattHendelse, "Registrert ForeldrepengerMottattHendelse")
        tilstand.håndter(this, foreldrepengerMottattHendelse)
    }

    fun håndter(overgangsstønadMottattHendelse: OvergangsstønadMottattHendelse) {
        if (journalpostId != overgangsstønadMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(overgangsstønadMottattHendelse, "Registrert OvergangsstønadMottattHendelse")
        tilstand.håndter(this, overgangsstønadMottattHendelse)
    }

    fun håndter(uføreMottattHendelse: UføreMottattHendelse) {
        if (journalpostId != uføreMottattHendelse.journalpostId()) return
        // Den påfølgende linja er viktig, fordi den blant annet kobler hendelsen sin aktivitetslogg
        // til Søker sin aktivitetslogg (Søker sin blir forelder)
        // Det gjør at alt som sendes inn i hendelsen sin aktivitetslogg ender opp i Søker sin også.
        kontekst(uføreMottattHendelse, "Registrert UføreMottattHendelse")
        tilstand.håndter(this, uføreMottattHendelse)
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
//            innsending.mottaTiltak(tiltakMottattHendelse)
        }

        fun håndter(innsending: Innsending, ytelserMottattHendelse: YtelserMottattHendelse) {
            innsending.mottaYtelser(ytelserMottattHendelse)
        }

        fun håndter(innsending: Innsending, foreldrepengerMottattHendelse: ForeldrepengerMottattHendelse) {
            innsending.mottaForeldrepengerVedtak(foreldrepengerMottattHendelse)
        }

        fun håndter(innsending: Innsending, overgangsstønadMottattHendelse: OvergangsstønadMottattHendelse) {
            innsending.mottaOvergangsstønadVedtak(overgangsstønadMottattHendelse)
        }

        fun håndter(innsending: Innsending, uføreMottattHendelse: UføreMottattHendelse) {
            innsending.mottaUføreVedtak(uføreMottattHendelse)
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
            personopplysningerMottattHendelse: PersonopplysningerMottattHendelse,
        ) {
            innsending.mottaPersonopplysninger(personopplysningerMottattHendelse)
            // Denne er flyttet inn i mottaPersonopplysninger for å vente med innhenting av Skjerming
            // til vi har fått svar fra pdl også ved oppdatering av fakta
            // innsending.trengerSkjermingdata(personopplysningerMottattHendelse)
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
//            innsending.mottaTiltak(tiltakMottattHendelse)
            innsending.trengerArenaYtelse(tiltakMottattHendelse)
            innsending.tilstand(tiltakMottattHendelse, AvventerYtelser)
        }
    }

    internal object AvventerYtelser : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerYtelser
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, ytelserMottattHendelse: YtelserMottattHendelse) {
            innsending.mottaYtelser(ytelserMottattHendelse)
            innsending.trengerForeldrepenger(ytelserMottattHendelse)
            innsending.tilstand(ytelserMottattHendelse, AvventerForeldrepenger)
        }
    }

    internal object AvventerForeldrepenger : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerForeldrepenger
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, foreldrepengerMottattHendelse: ForeldrepengerMottattHendelse) {
            foreldrepengerMottattHendelse.info("Fikk info om foreldrepenger: ${foreldrepengerMottattHendelse.foreldrepengerVedtakListe()}")
            innsending.mottaForeldrepengerVedtak(foreldrepengerMottattHendelse)
            innsending.trengerOvergangsstønad(foreldrepengerMottattHendelse)
            innsending.tilstand(foreldrepengerMottattHendelse, AvventerOvergangsstønad)
        }
    }

    internal object AvventerOvergangsstønad : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerOvergangsstønad
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, overgangsstønadMottattHendelse: OvergangsstønadMottattHendelse) {
            overgangsstønadMottattHendelse.info("Fikk info om overgangsstønad: ${overgangsstønadMottattHendelse.overgangsstønadVedtakListe()}")
            innsending.mottaOvergangsstønadVedtak(overgangsstønadMottattHendelse)
            innsending.trengerUføre(overgangsstønadMottattHendelse)
            innsending.tilstand(overgangsstønadMottattHendelse, AvventerUføre)
        }
    }

    internal object AvventerUføre : Tilstand {
        override val type: InnsendingTilstandType
            get() = InnsendingTilstandType.AvventerUføre
        override val timeout: Duration
            get() = Duration.ofDays(1)

        override fun håndter(innsending: Innsending, uføreMottattHendelse: UføreMottattHendelse) {
            uføreMottattHendelse.info("Fikk info om uføreVedtak: ${uføreMottattHendelse.uføreVedtak()}")
            innsending.mottaUføreVedtak(uføreMottattHendelse)
            innsending.tilstand(uføreMottattHendelse, InnsendingFerdigstilt)

            // TODO her kan vi trigge vilkårsvurdering av en behandling
            //      eller vi kan gjøre det fortløpende etter hver fakta innhenting
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
            // Denne har vi flyttet inn i mottaPersonopplysninger for å vente på pdl
            // innsending.trengerSkjermingdata(innsendingUtdatertHendelse)
            innsending.trengerArenaYtelse(innsendingUtdatertHendelse)
            innsending.trengerTiltak(innsendingUtdatertHendelse)
            innsending.trengerForeldrepenger(innsendingUtdatertHendelse)
            innsending.trengerOvergangsstønad(innsendingUtdatertHendelse)
            innsending.trengerUføre(innsendingUtdatertHendelse)
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
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.arenatiltak,
            melding = "Trenger arenatiltak",
            detaljer = mapOf("ident" to this.ident),
        )
    }

    private fun trengerArenaYtelse(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.arenaytelser,
            melding = "Trenger arenaytelser",
            detaljer = mapOf("ident" to this.ident),
        )
    }

    private fun trengerForeldrepenger(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.fpytelser,
            melding = "Trenger fpytelser",
            detaljer = mapOf(
                "ident" to this.ident,
                "fom" to this.filtreringsperiode().fra,
                "tom" to this.filtreringsperiode().til,
            ),
        )
    }

    private fun trengerOvergangsstønad(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.overgangsstønad,
            melding = "Trenger overgangsstønad",
            detaljer = mapOf(
                "ident" to this.ident,
                "fom" to this.filtreringsperiode().fra,
                "tom" to this.filtreringsperiode().til,
            ),
        )
    }

    private fun trengerUføre(hendelse: InnsendingHendelse) {
        hendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.uføre,
            melding = "Trenger uføre",
            detaljer = mapOf(
                "ident" to this.ident,
                "fom" to this.filtreringsperiode().fra,
                "tom" to this.filtreringsperiode().til,
            ),
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

    override fun opprettKontekst(): Kontekst = Kontekst(
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
                harBeskyttelsesbehovStrengtFortrolig,
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

    private fun mottaYtelser(ytelserMottattHendelse: YtelserMottattHendelse) {
        if (this.ytelser != null &&
            !ytelserMottattHendelse.tidsstempelYtelserInnhentet()
                .isAfter(this.ytelser!!.tidsstempelInnhentet)
        ) {
            ytelserMottattHendelse
                .info("Fikk utdatert info om arenaYtelser, lagrer ikke")
            return
        }

        ytelserMottattHendelse.info("Fikk info om arenaYtelser: ${ytelserMottattHendelse.ytelseSak()}")
        this.ytelser = InnhentedeArenaYtelser(
            tidsstempelInnhentet = ytelserMottattHendelse.tidsstempelYtelserInnhentet(),
            ytelserliste = ytelserMottattHendelse.ytelseSak().filter {
                val tomDato = it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX
                this.filtreringsperiode().overlapperMed(
                    Periode(
                        it.fomGyldighetsperiode.toLocalDate(),
                        tomDato.let { tom ->
                            if (tom < it.fomGyldighetsperiode.toLocalDate()) LocalDate.MAX else tom
                        },
                    ),
                )
            }.also {
                val antall = ytelserMottattHendelse.ytelseSak().size - it.size
                LOG.info { "Filtrerte bort $antall gamle ytelser" }
            },
        )
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
        // Dette er for at vi også skal hente skjerming ved oppdatering av fakta
        this.trengerSkjermingdata(personopplysningerMottattHendelse)
    }

//    private fun mottaTiltak(
//        tiltakMottattHendelse: TiltakMottattHendelse,
//    ) {
//        if (this.tiltak != null &&
//            !tiltakMottattHendelse.tidsstempelTiltakInnhentet()
//                .isAfter(this.tiltak!!.tidsstempelInnhentet)
//        ) {
//            tiltakMottattHendelse.info("Fikk utdatert info om tiltak, lagrer ikke")
//            return
//        }
//
//        fun earliest(fom: LocalDate?, tom: LocalDate?) =
//            when {
//                fom != null && tom != null -> if (tom.isBefore(fom)) {
//                    LOG.warn { "fom er etter tom, så vi bytter om de to datoene på tiltaket" }
//                    tom
//                } else {
//                    fom
//                }
//
//                else -> fom ?: LocalDate.MIN
//            }
//
//        fun latest(fom: LocalDate?, tom: LocalDate?) =
//            when {
//                fom != null && tom != null -> if (fom.isAfter(tom)) fom else tom
//                else -> tom ?: LocalDate.MAX
//            }
//
//        tiltakMottattHendelse
//            .info("Fikk info om tiltak: ${tiltakMottattHendelse.tiltaksaktivitet()}")
//
//        this.tiltak = InnhentedeTiltak(
//            tidsstempelInnhentet = tiltakMottattHendelse.tidsstempelTiltakInnhentet(),
//            tiltaksliste = tiltakMottattHendelse.tiltaksaktivitet().filter {
//                LOG.info { "filtreringsperiode : ${this.filtreringsperiode()}" }
//                LOG.info { "startDato : ${it.deltakelseFom}" }
//                LOG.info { "sluttDato : ${it.deltakelseTom}" }
//                val periode = Periode(
//                    earliest(it.deltakelseFom, it.deltakelseTom),
//                    latest(it.deltakelseFom, it.deltakelseTom),
//                )
//                LOG.info { "periode : $periode" }
//                this.filtreringsperiode().overlapperMed(periode)
//            }.also {
//                val antall = tiltakMottattHendelse.tiltaksaktivitet().size - it.size
//                LOG.info { "Filtrerte bort $antall gamle tiltak" }
//            },
//        )
//    }

    private fun mottaForeldrepengerVedtak(
        foreldrepengerMottattHendelse: ForeldrepengerMottattHendelse,
    ) {
        if (this.foreldrepengerVedtak != null &&
            !foreldrepengerMottattHendelse.tidsstempelForeldrepengerVedtakInnhentet()
                .isAfter(this.foreldrepengerVedtak!!.tidsstempelInnhentet)
        ) {
            foreldrepengerMottattHendelse.info("Fikk utdatert info om foreldrepengervedtak, lagrer ikke")
            return
        }

        foreldrepengerMottattHendelse
            .info("Fikk info om foreldrepengerVedtak: ${foreldrepengerMottattHendelse.foreldrepengerVedtakListe()}")

        this.foreldrepengerVedtak = InnhentedeForeldrepengerVedtak(
            foreldrepengerVedtakliste = foreldrepengerMottattHendelse.foreldrepengerVedtakListe(),
            tidsstempelInnhentet = foreldrepengerMottattHendelse.tidsstempelForeldrepengerVedtakInnhentet(),
        )
    }

    private fun mottaOvergangsstønadVedtak(
        overgangsstønadMottattHendelse: OvergangsstønadMottattHendelse,
    ) {
        if (this.overgangsstønadVedtak != null &&
            !overgangsstønadMottattHendelse.tidsstempelOvergangsstønadVedtakInnhentet()
                .isAfter(this.overgangsstønadVedtak!!.tidsstempelInnhentet)
        ) {
            overgangsstønadMottattHendelse.info("Fikk utdatert info om overgangsstønadvedtak, lagrer ikke")
            return
        }

        overgangsstønadMottattHendelse
            .info("Fikk info om overgangsstønadVedtak: ${overgangsstønadMottattHendelse.overgangsstønadVedtakListe()}")

        this.overgangsstønadVedtak = InnhentedeOvergangsstønadVedtak(
            overgangsstønadVedtak = overgangsstønadMottattHendelse.overgangsstønadVedtakListe(),
            tidsstempelInnhentet = overgangsstønadMottattHendelse.tidsstempelOvergangsstønadVedtakInnhentet(),
        )
    }

    private fun mottaUføreVedtak(
        uføreMottattHendelse: UføreMottattHendelse,
    ) {
        if (this.uføreVedtak != null &&
            !uføreMottattHendelse.tidsstempelUføreVedtakInnhentet()
                .isAfter(this.uføreVedtak!!.tidsstempelInnhentet)
        ) {
            uføreMottattHendelse.info("Fikk utdatert info om uføreVedtak, lagrer ikke")
            return
        }

        uføreMottattHendelse
            .info("Fikk info om uføreMottattHendelse: ${uføreMottattHendelse.uføreVedtak()}")

        if (uføreMottattHendelse.uføreVedtak().virkDato != null) {
            if (!this.filtreringsperiode().inneholder(uføreMottattHendelse.uføreVedtak().virkDato!!)) {
                uføreMottattHendelse
                    .info("Filtrer bort vedtak da virkDato ikke er i vurderingsperioden ${this.filtreringsperiode()}")
                this.uføreVedtak = InnhentetUføre(
                    uføreVedtak = null,
                    tidsstempelInnhentet = uføreMottattHendelse.tidsstempelUføreVedtakInnhentet(),
                )
                return
            }
        }

        this.uføreVedtak = InnhentetUføre(
            uføreVedtak = uføreMottattHendelse.uføreVedtak(),
            tidsstempelInnhentet = uføreMottattHendelse.tidsstempelUføreVedtakInnhentet(),
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
                    is Personopplysninger.BarnMedIdent -> it.copy(
                        skjermet = skjermingMottattHendelse.skjerming()
                            .barn.firstOrNull { barn -> barn.ident == it.ident }?.skjerming,
                    )

                    is Personopplysninger.BarnUtenIdent -> it
                    is Personopplysninger.Søker -> it.copy(
                        skjermet = skjermingMottattHendelse.skjerming().søker.skjerming,
                    )
                }
            },
        )
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
