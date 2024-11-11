package no.nav.tiltakspenger.meldekort.domene

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.felles.Navkontor
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.Saksbehandler
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

sealed interface Meldekort {
    val id: MeldekortId
    val sakId: SakId
    val saksnummer: Saksnummer
    val fnr: Fnr
    val rammevedtakId: VedtakId
    val forrigeMeldekortId: MeldekortId?
    val meldeperiode: Meldeperiode
    val tiltakstype: TiltakstypeSomGirRett
    val fraOgMed: LocalDate get() = meldeperiode.fraOgMed
    val tilOgMed: LocalDate get() = meldeperiode.tilOgMed
    val periode: Periode get() = meldeperiode.periode
    val saksbehandler: String?
    val beslutter: String?
    val status: MeldekortStatus
    val navkontor: Navkontor?
    val iverksattTidspunkt: LocalDateTime?

    /** Vil være duplikat av det siste vedtaket som påvirker denne meldeperioden. Vil være et førstegangsvedtak i MVP, men vil på sikt også stamme fra revurderinger. */
    val antallDagerForMeldeperiode: Int

    /** Totalsummen for meldeperioden */
    val beløpTotal: Int?

    val meldeperiodeId: MeldeperiodeId get() = MeldeperiodeId.fraPeriode(periode)

    /**
     * Meldekort utfylt av saksbehandler og godkjent av beslutter.
     * Når veileder/bruker har fylt ut meldekortet vil ikke denne klassen kunne gjenbrukes uten endringer. Kanskje vi må ha en egen klasse for veileder-/brukerutfylt meldekort.
     *
     * @param saksbehandler: Obligatorisk dersom meldekortet er utfylt av saksbehandler.
     * @param beslutter: Obligatorisk dersom meldekortet er godkjent av beslutter.
     * @param forrigeMeldekortId kan være null dersom det er første meldekort.
     */
    data class UtfyltMeldekort(
        override val id: MeldekortId,
        override val meldeperiodeId: MeldeperiodeId,
        override val sakId: SakId,
        override val saksnummer: Saksnummer,
        override val fnr: Fnr,
        override val rammevedtakId: VedtakId,
        override val forrigeMeldekortId: MeldekortId?,
        override val meldeperiode: Meldeperiode.UtfyltMeldeperiode,
        override val tiltakstype: TiltakstypeSomGirRett,
        override val saksbehandler: String,
        override val beslutter: String?,
        override val status: MeldekortStatus,
        override val iverksattTidspunkt: LocalDateTime?,
        override val navkontor: Navkontor,
        override val antallDagerForMeldeperiode: Int,
    ) : Meldekort {

        init {
            require(status in listOf(MeldekortStatus.GODKJENT, MeldekortStatus.KLAR_TIL_BESLUTNING))
            if (status == MeldekortStatus.GODKJENT) {
                requireNotNull(iverksattTidspunkt)
                requireNotNull(beslutter)
            } else {
                require(iverksattTidspunkt == null)
                require(beslutter == null)
            }
        }

        /**
         * TODO post-mvp jah: Ved revurderinger av rammevedtaket, så må vi basere oss på både forrige meldekort og revurderingsvedtaket. Dette løser vi å flytte mer logikk til Sak.kt.
         * TODO post-mvp jah: Når vi implementerer delvis innvilgelse vil hele meldekortperioder kunne bli SPERRET.
         */
        fun opprettNesteMeldekort(
            utfallsperioder: Periodisering<AvklartUtfallForPeriode>,
        ): Either<SisteMeldekortErUtfylt, IkkeUtfyltMeldekort> {
            val periode = Periode(fraOgMed.plusDays(14), tilOgMed.plusDays(14))
            if (periode.tilOgMed.isAfter(utfallsperioder.totalePeriode.tilOgMed)) {
                return SisteMeldekortErUtfylt.left()
            }
            val meldekortId = MeldekortId.random()
            return IkkeUtfyltMeldekort(
                id = meldekortId,
                meldeperiodeId = MeldeperiodeId.fraPeriode(periode),
                sakId = this.sakId,
                saksnummer = this.saksnummer,
                fnr = this.fnr,
                rammevedtakId = this.rammevedtakId,
                forrigeMeldekortId = this.id,
                tiltakstype = this.tiltakstype,
                navkontor = this.navkontor,
                meldeperiode = Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
                    meldeperiode = periode,
                    tiltakstype = this.tiltakstype,
                    meldekortId = meldekortId,
                    sakId = this.sakId,
                    utfallsperioder = utfallsperioder,
                ),
                antallDagerForMeldeperiode = this.antallDagerForMeldeperiode,
            ).right()
        }

        fun iverksettMeldekort(
            beslutter: Saksbehandler,
        ): Either<KanIkkeIverksetteMeldekort, UtfyltMeldekort> {
            if (!beslutter.erBeslutter()) {
                return KanIkkeIverksetteMeldekort.MåVæreBeslutter(beslutter.roller).left()
            }
            if (saksbehandler == beslutter.navIdent) {
                return KanIkkeIverksetteMeldekort.SaksbehandlerOgBeslutterKanIkkeVæreLik.left()
            }
            require(status == MeldekortStatus.KLAR_TIL_BESLUTNING)
            require(this.beslutter == null)
            return this.copy(
                beslutter = beslutter.navIdent,
                status = MeldekortStatus.GODKJENT,
                iverksattTidspunkt = LocalDateTime.now(),
            ).right()
        }

        override val beløpTotal: Int = meldeperiode.beregnTotalbeløp()
    }

    data class IkkeUtfyltMeldekort(
        override val id: MeldekortId,
        override val meldeperiodeId: MeldeperiodeId,
        override val sakId: SakId,
        override val saksnummer: Saksnummer,
        override val fnr: Fnr,
        override val rammevedtakId: VedtakId,
        override val forrigeMeldekortId: MeldekortId?,
        override val tiltakstype: TiltakstypeSomGirRett,
        override val meldeperiode: Meldeperiode.IkkeUtfyltMeldeperiode,
        override val navkontor: Navkontor?,
        override val antallDagerForMeldeperiode: Int,
    ) : Meldekort {
        override val iverksattTidspunkt = null

        override val beløpTotal = null
        override val status = MeldekortStatus.IKKE_UTFYLT

        fun sendTilBeslutter(
            utfyltMeldeperiode: Meldeperiode.UtfyltMeldeperiode,
            saksbehandler: Saksbehandler,
            navkontor: Navkontor,
        ): Either<KanIkkeSendeMeldekortTilBeslutter, UtfyltMeldekort> {
            require(utfyltMeldeperiode.periode == this.periode) {
                "Når man fyller ut et meldekort må meldekortperioden være den samme som den som er opprettet. Opprettet periode: ${this.meldeperiode.periode}, utfylt periode: ${utfyltMeldeperiode.periode}"
            }
            if (!saksbehandler.erSaksbehandler()) {
                return KanIkkeSendeMeldekortTilBeslutter.MåVæreSaksbehandler(saksbehandler.roller).left()
            }
            if (!erKlarTilUtfylling()) {
                // John har avklart med Sølvi og Taulant at vi bør ha en begrensning på at vi kan fylle ut et meldekort hvis dagens dato er innenfor meldekortperioden eller senere.
                // Dette kan endres på ved behov.
                return KanIkkeSendeMeldekortTilBeslutter.MeldekortperiodenKanIkkeVæreFremITid.left()
            }
            return UtfyltMeldekort(
                id = this.id,
                meldeperiodeId = this.meldeperiodeId,
                sakId = this.sakId,
                saksnummer = this.saksnummer,
                fnr = this.fnr,
                rammevedtakId = this.rammevedtakId,
                forrigeMeldekortId = this.forrigeMeldekortId,
                meldeperiode = utfyltMeldeperiode,
                tiltakstype = this.tiltakstype,
                saksbehandler = saksbehandler.navIdent,
                beslutter = this.beslutter,
                status = MeldekortStatus.KLAR_TIL_BESLUTNING,
                iverksattTidspunkt = null,
                navkontor = navkontor,
                antallDagerForMeldeperiode = this.antallDagerForMeldeperiode,
            ).right()
        }

        override val beslutter = null
        override val saksbehandler = null

        fun erKlarTilUtfylling(): Boolean {
            return !LocalDate.now().isBefore(periode.fraOgMed)
        }
    }
}

fun Rammevedtak.opprettFørsteMeldekortForEnSak(): Meldekort.IkkeUtfyltMeldekort {
    val periode = finnFørsteMeldekortsperiode(this.periode)
    val meldekortId = MeldekortId.random()
    val tiltakstype = this.behandling.vilkårssett.tiltakDeltagelseVilkår.registerSaksopplysning.tiltakstype
    val utfallsperioder: Periodisering<AvklartUtfallForPeriode> = this.utfallsperioder

    return Meldekort.IkkeUtfyltMeldekort(
        id = meldekortId,
        meldeperiodeId = MeldeperiodeId.fraPeriode(periode),
        sakId = this.sakId,
        saksnummer = this.saksnummer,
        fnr = this.behandling.fnr,
        rammevedtakId = this.id,
        forrigeMeldekortId = null,
        tiltakstype = tiltakstype,
        // TODO post-mvp: Her har vi mulighet til å hente verdien fra brukers geografiske tilhørighet + norg2.
        navkontor = null,
        meldeperiode = Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
            meldeperiode = periode,
            utfallsperioder = utfallsperioder,
            tiltakstype = tiltakstype,
            meldekortId = meldekortId,
            sakId = this.sakId,
        ),
        antallDagerForMeldeperiode = this.behandling.antallDagerPerMeldeperiode,
    )
}

fun finnFørsteMeldekortsperiode(periode: Periode): Periode {
    val førsteMandagIMeldekortsperiode = periode.fraOgMed.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sisteSøndagIMeldekortsperiode = førsteMandagIMeldekortsperiode.plusDays(13)

    return Periode(førsteMandagIMeldekortsperiode, sisteSøndagIMeldekortsperiode)
}
