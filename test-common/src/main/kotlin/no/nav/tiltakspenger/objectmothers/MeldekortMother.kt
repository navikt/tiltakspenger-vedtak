package no.nav.tiltakspenger.objectmothers

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.common.getOrFail
import no.nav.tiltakspenger.felles.Navkontor
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.erHelg
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.MeldekortStatus
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import no.nav.tiltakspenger.meldekort.domene.Meldeperiode
import no.nav.tiltakspenger.meldekort.domene.MeldeperiodeId
import no.nav.tiltakspenger.meldekort.domene.Meldeperioder
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import java.time.LocalDate
import java.time.LocalDateTime

interface MeldekortMother {

    fun utfyltMeldekort(
        id: MeldekortId = MeldekortId.random(),
        sakId: SakId = SakId.random(),
        fnr: Fnr = Fnr.random(),
        rammevedtakId: VedtakId = VedtakId.random(),
        meldekortperiode: Meldeperiode.UtfyltMeldeperiode =
            utfyltMeldekortperiode(
                meldekortId = id,
                sakId = sakId,
            ),
        meldeperiodeId: MeldeperiodeId = MeldeperiodeId.fraPeriode(meldekortperiode.periode),
        saksbehandler: String = "saksbehandler",
        beslutter: String = "beslutter",
        forrigeMeldekortId: MeldekortId? = null,
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
        tiltaksnavn: String = "Arbeidsmarkedsoppfølging - gruppe",
        status: MeldekortStatus = MeldekortStatus.GODKJENT,
        iverksattTidspunkt: LocalDateTime? = null,
        navkontor: Navkontor = ObjectMother.navkontor(),
    ) = Meldekort.UtfyltMeldekort(
        id = id,
        meldeperiodeId = meldeperiodeId,
        sakId = sakId,
        fnr = fnr,
        rammevedtakId = rammevedtakId,
        meldeperiode = meldekortperiode,
        saksbehandler = saksbehandler,
        beslutter = beslutter,
        forrigeMeldekortId = forrigeMeldekortId,
        tiltakstype = tiltakstype,
        tiltaksnavn = tiltaksnavn,
        status = status,
        iverksattTidspunkt = iverksattTidspunkt,
        navkontor = navkontor,
    )

    /**
     * @param startDato Må starte på en mandag.
     */
    fun utfyltMeldekortperiode(
        sakId: SakId = SakId.random(),
        startDato: LocalDate = LocalDate.of(2023, 1, 2),
        meldekortId: MeldekortId = MeldekortId.random(),
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
    ): Meldeperiode.UtfyltMeldeperiode =
        Meldeperiode.UtfyltMeldeperiode(
            sakId,
            maksAntallDeltattTiltaksdagerIMeldekortperiode(startDato, meldekortId, tiltakstype),
        )

    fun maksAntallDeltattTiltaksdagerIMeldekortperiode(
        startDato: LocalDate,
        meldekortId: MeldekortId,
        tiltakstype: TiltakstypeSomGirRett,
    ): NonEmptyList<Meldekortdag.Utfylt> =
        (
            tiltaksdager(startDato, meldekortId, tiltakstype) +
                ikkeTiltaksdager(startDato.plusDays(5), meldekortId, 2, tiltakstype) +
                tiltaksdager(startDato.plusDays(7), meldekortId, tiltakstype) +
                ikkeTiltaksdager(startDato.plusDays(12), meldekortId, 2, tiltakstype)
            ).toNonEmptyListOrNull()!!

    fun tiltaksdager(
        startDato: LocalDate = LocalDate.of(2023, 1, 2),
        meldekortId: MeldekortId = MeldekortId.random(),
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
        tiltaksnavn: String = "Arbeidsmarkedsoppfølging - gruppe",
        antallDager: Int = 5,
    ): NonEmptyList<Meldekortdag.Utfylt.Deltatt.DeltattUtenLønnITiltaket> {
        require(antallDager in 1..5) {
            "Antall sammenhengende dager vil aldri være mer mindre enn 1 eller mer enn 5, men var $antallDager"
        }
        return List(antallDager) { index ->
            Meldekortdag.Utfylt.Deltatt.DeltattUtenLønnITiltaket.create(
                dato = startDato.plusDays(index.toLong()),
                meldekortId = meldekortId,
                tiltakstype = tiltakstype,
                tiltaksnavn = tiltaksnavn,
            )
        }.toNonEmptyListOrNull()!!
    }

    fun ikkeTiltaksdager(
        startDato: LocalDate = LocalDate.of(2023, 1, 2),
        meldekortId: MeldekortId = MeldekortId.random(),
        antallDager: Int = 2,
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
        tiltaksnavn: String = "Arbeidsmarkedsoppfølging - gruppe",
    ): NonEmptyList<Meldekortdag.Utfylt.IkkeDeltatt> {
        require(antallDager in 1..5) {
            "Antall sammenhengende dager vil aldri være mer mindre enn 1 eller mer enn 5, men var $antallDager"
        }
        return List(antallDager) { index ->
            Meldekortdag.Utfylt.IkkeDeltatt.create(
                dato = startDato.plusDays(index.toLong()),
                meldekortId = meldekortId,
                tiltakstype = tiltakstype,
                tiltaksnavn = tiltaksnavn,
            )
        }.toNonEmptyListOrNull()!!
    }

    fun beregnMeldekortperioder(
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
        tiltaksnavn: String = "Arbeidsrettet oppfølging - gruppe",
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
        sakId: SakId = SakId.random(),
        fnr: Fnr = Fnr.random(),
        perioder: NonEmptyList<NonEmptyList<SendMeldekortTilBeslutterKommando.Dag>>,
        rammevedtakId: VedtakId = VedtakId.random(),
        utfallsperioder: Periodisering<AvklartUtfallForPeriode> = Periodisering(
            initiellVerdi = AvklartUtfallForPeriode.OPPFYLT,
            totalePeriode = Periode(perioder.first().first().dag, perioder.first().last().dag),
        ),
        navkontor: Navkontor = ObjectMother.navkontor(),
    ): Meldeperioder {
        val kommandoer = perioder.map { dager ->
            SendMeldekortTilBeslutterKommando(
                sakId = sakId,
                meldekortId = MeldekortId.random(),
                saksbehandler = saksbehandler,
                dager = dager,
                correlationId = CorrelationId.generate(),
                navkontor = navkontor,
            )
        }
        return kommandoer.drop(1).fold(
            førsteBeregnetMeldekort(
                tiltakstype = tiltakstype,
                tiltaksnavn = tiltaksnavn,
                meldekortId = kommandoer.first().meldekortId,
                sakId = sakId,
                fnr = fnr,
                rammevedtakId = rammevedtakId,
                kommando = kommandoer.first(),
                utfallsperioder = utfallsperioder,
                meldeperiodeId = MeldeperiodeId.fraPeriode(kommandoer.first().periode),
            ).first,
        ) { meldekortperioder, kommando ->
            meldekortperioder.beregnNesteMeldekort(kommando, fnr)
        }
    }

    fun førsteBeregnetMeldekort(
        tiltakstype: TiltakstypeSomGirRett,
        tiltaksnavn: String,
        meldekortId: MeldekortId,
        sakId: SakId,
        fnr: Fnr = Fnr.random(),
        rammevedtakId: VedtakId,
        kommando: SendMeldekortTilBeslutterKommando,
        meldeperiodeId: MeldeperiodeId = MeldeperiodeId.fraPeriode(kommando.periode),
        utfallsperioder: Periodisering<AvklartUtfallForPeriode>,
        navkontor: Navkontor = ObjectMother.navkontor(),
    ) = Meldeperioder(
        tiltakstype = tiltakstype,
        verdi = nonEmptyListOf(
            Meldekort.IkkeUtfyltMeldekort(
                id = meldekortId,
                meldeperiodeId = meldeperiodeId,
                sakId = sakId,
                fnr = fnr,
                rammevedtakId = rammevedtakId,
                forrigeMeldekortId = null,
                tiltakstype = tiltakstype,
                tiltaksnavn = tiltaksnavn,
                navkontor = navkontor,
                meldeperiode = Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
                    sakId = sakId,
                    meldeperiode = kommando.periode,
                    utfallsperioder = utfallsperioder,
                    tiltakstype = tiltakstype,
                    tiltaksnavn = tiltaksnavn,
                    meldekortId = meldekortId,
                ),
            ),
        ),
    ).sendTilBeslutter(kommando).getOrFail()

    fun Meldeperioder.beregnNesteMeldekort(
        kommando: SendMeldekortTilBeslutterKommando,
        fnr: Fnr,
        meldeperiodeId: MeldeperiodeId = MeldeperiodeId.fraPeriode(kommando.periode),
        navkontor: Navkontor = ObjectMother.navkontor(),
    ): Meldeperioder {
        val meldekortId = kommando.meldekortId
        val sakId = kommando.sakId
        val rammevedtakId = VedtakId.random()
        val tiltakstype = TiltakstypeSomGirRett.GRUPPE_AMO
        val tiltaksnavn = "Arbeidsrettet oppfølging - gruppe"
        val utfallsperioder = Periodisering(
            initiellVerdi = AvklartUtfallForPeriode.OPPFYLT,
            totalePeriode = Periode(kommando.dager.first().dag, kommando.dager.last().dag),
        )
        return Meldeperioder(
            tiltakstype = tiltakstype,
            verdi = this.verdi + Meldekort.IkkeUtfyltMeldekort(
                id = meldekortId,
                meldeperiodeId = meldeperiodeId,
                sakId = sakId,
                fnr = fnr,
                rammevedtakId = rammevedtakId,
                forrigeMeldekortId = this.verdi.last().id,
                tiltakstype = tiltakstype,
                tiltaksnavn = tiltaksnavn,
                navkontor = navkontor,
                meldeperiode = Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
                    sakId = sakId,
                    meldeperiode = kommando.periode,
                    utfallsperioder = utfallsperioder,
                    tiltakstype = tiltakstype,
                    tiltaksnavn = tiltaksnavn,
                    meldekortId = meldekortId,
                ),
            ),
        ).sendTilBeslutter(kommando).getOrFail().first
    }
}

fun Meldekort.IkkeUtfyltMeldekort.tilSendMeldekortTilBeslutterKommando(
    saksbehandler: Saksbehandler,
    navkontor: Navkontor = this.navkontor ?: ObjectMother.navkontor(),
): SendMeldekortTilBeslutterKommando {
    val dager = meldeperiode.map { dag ->
        SendMeldekortTilBeslutterKommando.Dag(
            dag = dag.dato,
            status = when (dag) {
                is Meldekortdag.IkkeUtfylt -> if (dag.dato.erHelg()) {
                    SendMeldekortTilBeslutterKommando.Status.IKKE_DELTATT
                } else {
                    SendMeldekortTilBeslutterKommando.Status.DELTATT_UTEN_LØNN_I_TILTAKET
                }

                is Meldekortdag.Utfylt -> when (dag) {
                    is Meldekortdag.Utfylt.Deltatt.DeltattMedLønnITiltaket -> SendMeldekortTilBeslutterKommando.Status.DELTATT_MED_LØNN_I_TILTAKET
                    is Meldekortdag.Utfylt.Deltatt.DeltattUtenLønnITiltaket -> SendMeldekortTilBeslutterKommando.Status.DELTATT_UTEN_LØNN_I_TILTAKET
                    is Meldekortdag.Utfylt.Fravær.Syk.SykBruker -> SendMeldekortTilBeslutterKommando.Status.FRAVÆR_SYK
                    is Meldekortdag.Utfylt.Fravær.Syk.SyktBarn -> SendMeldekortTilBeslutterKommando.Status.FRAVÆR_SYKT_BARN
                    is Meldekortdag.Utfylt.Fravær.Velferd.VelferdGodkjentAvNav -> SendMeldekortTilBeslutterKommando.Status.FRAVÆR_VELFERD_GODKJENT_AV_NAV
                    is Meldekortdag.Utfylt.Fravær.Velferd.VelferdIkkeGodkjentAvNav -> SendMeldekortTilBeslutterKommando.Status.FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV
                    is Meldekortdag.Utfylt.IkkeDeltatt -> SendMeldekortTilBeslutterKommando.Status.IKKE_DELTATT
                    is Meldekortdag.Utfylt.Sperret -> SendMeldekortTilBeslutterKommando.Status.SPERRET
                }
            },
        )
    }
    return SendMeldekortTilBeslutterKommando(
        sakId = sakId,
        meldekortId = id,
        saksbehandler = saksbehandler,
        dager = dager,
        correlationId = CorrelationId.generate(),
        navkontor = navkontor,
    )
}
