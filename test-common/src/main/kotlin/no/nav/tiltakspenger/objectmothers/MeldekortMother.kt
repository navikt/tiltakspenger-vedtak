package no.nav.tiltakspenger.objectmothers

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.common.getOrFail
import no.nav.tiltakspenger.felles.Saksbehandler
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
import no.nav.tiltakspenger.meldekort.domene.Meldeperioder
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import java.time.LocalDate

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
        saksbehandler: String = "saksbehandler",
        beslutter: String = "beslutter",
        forrigeMeldekortId: MeldekortId? = null,
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
        status: MeldekortStatus = MeldekortStatus.GODKJENT,
    ) = Meldekort.UtfyltMeldekort(
        id = id,
        sakId = sakId,
        fnr = fnr,
        rammevedtakId = rammevedtakId,
        meldekortperiode = meldekortperiode,
        saksbehandler = saksbehandler,
        beslutter = beslutter,
        forrigeMeldekortId = forrigeMeldekortId,
        tiltakstype = tiltakstype,
        status = status,
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
        antallDager: Int = 5,
    ): NonEmptyList<Meldekortdag.Utfylt.Deltatt.DeltattUtenLønnITiltaket> {
        require(antallDager in 1..5) {
            "Antall sammenhengende dager vil aldri være mer mindre enn 1 eller mer enn 5, men var $antallDager"
        }
        return List(antallDager) { index ->
            Meldekortdag.Utfylt.Deltatt.DeltattUtenLønnITiltaket(
                dato = startDato.plusDays(index.toLong()),
                meldekortId = meldekortId,
                tiltakstype = tiltakstype,
            )
        }.toNonEmptyListOrNull()!!
    }

    fun ikkeTiltaksdager(
        startDato: LocalDate = LocalDate.of(2023, 1, 2),
        meldekortId: MeldekortId = MeldekortId.random(),
        antallDager: Int = 2,
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
    ): NonEmptyList<Meldekortdag.Utfylt.IkkeDeltatt> {
        require(antallDager in 1..5) {
            "Antall sammenhengende dager vil aldri være mer mindre enn 1 eller mer enn 5, men var $antallDager"
        }
        return List(antallDager) { index ->
            Meldekortdag.Utfylt.IkkeDeltatt(
                dato = startDato.plusDays(index.toLong()),
                meldekortId = meldekortId,
                tiltakstype = tiltakstype,
            )
        }.toNonEmptyListOrNull()!!
    }

    fun beregnMeldekortperioder(
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
        sakId: SakId = SakId.random(),
        fnr: Fnr = Fnr.random(),
        perioder: NonEmptyList<NonEmptyList<SendMeldekortTilBeslutterKommando.Dag>>,
        rammevedtakId: VedtakId = VedtakId.random(),
        utfallsperioder: Periodisering<AvklartUtfallForPeriode> = Periodisering(
            initiellVerdi = AvklartUtfallForPeriode.OPPFYLT,
            totalePeriode = Periode(perioder.first().first().dag, perioder.first().last().dag),
        ),
    ): Meldeperioder {
        val kommandoer = perioder.map { dager ->
            SendMeldekortTilBeslutterKommando(
                sakId = sakId,
                meldekortId = MeldekortId.random(),
                saksbehandler = saksbehandler,
                dager = dager,
            )
        }
        return kommandoer.drop(1).fold(
            førsteBeregnetMeldekort(tiltakstype, kommandoer.first().meldekortId, sakId, fnr, rammevedtakId, kommandoer.first(), utfallsperioder).first,
        ) { meldekortperioder, kommando ->
            meldekortperioder.beregnNesteMeldekort(kommando, fnr)
        }
    }

    fun førsteBeregnetMeldekort(
        tiltakstype: TiltakstypeSomGirRett,
        meldekortId: MeldekortId,
        sakId: SakId,
        fnr: Fnr = Fnr.random(),
        rammevedtakId: VedtakId,
        kommando: SendMeldekortTilBeslutterKommando,
        utfallsperioder: Periodisering<AvklartUtfallForPeriode>,
    ) = Meldeperioder(
        tiltakstype = tiltakstype,
        verdi = nonEmptyListOf(
            Meldekort.IkkeUtfyltMeldekort(
                id = meldekortId,
                sakId = sakId,
                fnr = fnr,
                rammevedtakId = rammevedtakId,
                forrigeMeldekortId = null,
                tiltakstype = tiltakstype,
                meldekortperiode = Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
                    sakId = sakId,
                    meldeperiode = kommando.periode,
                    utfallsperioder = utfallsperioder,
                    tiltakstype = tiltakstype,
                    meldekortId = meldekortId,
                ),
            ),
        ),
    ).sendTilBeslutter(kommando).getOrFail()

    fun Meldeperioder.beregnNesteMeldekort(
        kommando: SendMeldekortTilBeslutterKommando,
        fnr: Fnr,
    ): Meldeperioder {
        val meldekortId = kommando.meldekortId
        val sakId = kommando.sakId
        val rammevedtakId = VedtakId.random()
        val tiltakstype = TiltakstypeSomGirRett.GRUPPE_AMO
        val utfallsperioder = Periodisering(
            initiellVerdi = AvklartUtfallForPeriode.OPPFYLT,
            totalePeriode = Periode(kommando.dager.first().dag, kommando.dager.last().dag),
        )
        return Meldeperioder(
            tiltakstype = tiltakstype,
            verdi = this.verdi + Meldekort.IkkeUtfyltMeldekort(
                id = meldekortId,
                sakId = sakId,
                fnr = fnr,
                rammevedtakId = rammevedtakId,
                forrigeMeldekortId = null,
                tiltakstype = tiltakstype,
                meldekortperiode = Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
                    sakId = sakId,
                    meldeperiode = kommando.periode,
                    utfallsperioder = utfallsperioder,
                    tiltakstype = tiltakstype,
                    meldekortId = meldekortId,
                ),
            ),
        ).sendTilBeslutter(kommando).getOrFail().first
    }
}
