package no.nav.tiltakspenger.meldekort.domene

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.felles.Navkontor
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

sealed interface Meldekort {
    val id: MeldekortId
    val sakId: SakId
    val fnr: Fnr
    val rammevedtakId: VedtakId
    val forrigeMeldekortId: MeldekortId?
    val meldeperiode: Meldeperiode
    val tiltakstype: TiltakstypeSomGirRett
    val tiltaksnavn: String
    val fraOgMed: LocalDate get() = meldeperiode.fraOgMed
    val tilOgMed: LocalDate get() = meldeperiode.tilOgMed
    val periode: Periode get() = meldeperiode.periode
    val saksbehandler: String?
    val beslutter: String?
    val status: MeldekortStatus
    val navkontor: Navkontor?

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
        override val fnr: Fnr,
        override val rammevedtakId: VedtakId,
        override val forrigeMeldekortId: MeldekortId?,
        override val meldeperiode: Meldeperiode.UtfyltMeldeperiode,
        override val tiltakstype: TiltakstypeSomGirRett,
        override val tiltaksnavn: String,
        override val saksbehandler: String,
        override val beslutter: String?,
        override val status: MeldekortStatus,
        val iverksattTidspunkt: LocalDateTime?,
        override val navkontor: Navkontor,
    ) : Meldekort {

        init {
            require(status in listOf(MeldekortStatus.GODKJENT, MeldekortStatus.KLAR_TIL_BESLUTNING))
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
                fnr = this.fnr,
                rammevedtakId = this.rammevedtakId,
                forrigeMeldekortId = this.id,
                tiltakstype = this.tiltakstype,
                tiltaksnavn = this.tiltaksnavn,
                navkontor = this.navkontor,
                meldeperiode =
                Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
                    meldeperiode = periode,
                    tiltakstype = this.tiltakstype,
                    tiltaksnavn = this.tiltaksnavn,
                    meldekortId = meldekortId,
                    sakId = this.sakId,
                    utfallsperioder = utfallsperioder,
                ),
            ).right()
        }

        fun iverksettMeldekort(beslutter: Saksbehandler): Either<KanIkkeIverksetteMeldekort, UtfyltMeldekort> {
            if (!beslutter.isBeslutter()) {
                return KanIkkeIverksetteMeldekort.MåVæreBeslutter(beslutter.roller).left()
            }
            if (saksbehandler == beslutter.navIdent) {
                return KanIkkeIverksetteMeldekort.SaksbehandlerOgBeslutterKanIkkeVæreLik.left()
            }
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
        override val fnr: Fnr,
        override val rammevedtakId: VedtakId,
        override val forrigeMeldekortId: MeldekortId?,
        override val tiltakstype: TiltakstypeSomGirRett,
        override val tiltaksnavn: String,
        override val meldeperiode: Meldeperiode.IkkeUtfyltMeldeperiode,
        override val navkontor: Navkontor?,
    ) : Meldekort {
        override val beløpTotal = null
        override val status = MeldekortStatus.KLAR_TIL_UTFYLLING
        fun sendTilBeslutter(
            meldekortperiode: Meldeperiode.UtfyltMeldeperiode,
            saksbehandler: Saksbehandler,
            navkontor: Navkontor,
        ): Either<KanIkkeSendeMeldekortTilBeslutter, UtfyltMeldekort> {
            if (!saksbehandler.isSaksbehandler()) {
                return KanIkkeSendeMeldekortTilBeslutter.MåVæreSaksbehandler(saksbehandler.roller).left()
            }
            if (LocalDate.now().isBefore(meldekortperiode.periode.fraOgMed)) {
                // John har avklart med Sølvi og Taulant at vi bør ha en begrensning på at vi kan fylle ut et meldekort hvis dagens dato er innenfor meldekortperioden eller senere.
                // Dette kan endres på ved behov.
                return KanIkkeSendeMeldekortTilBeslutter.MeldekortperiodenKanIkkeVæreFremITid.left()
            }
            return UtfyltMeldekort(
                id = this.id,
                meldeperiodeId = this.meldeperiodeId,
                sakId = this.sakId,
                fnr = this.fnr,
                rammevedtakId = this.rammevedtakId,
                forrigeMeldekortId = this.forrigeMeldekortId,
                meldeperiode = meldekortperiode,
                tiltakstype = this.tiltakstype,
                tiltaksnavn = this.tiltaksnavn,
                saksbehandler = saksbehandler.navIdent,
                beslutter = this.beslutter,
                status = MeldekortStatus.KLAR_TIL_BESLUTNING,
                iverksattTidspunkt = null,
                navkontor = navkontor,
            ).right()
        }

        override val beslutter = null
        override val saksbehandler = null
    }
}

fun Rammevedtak.opprettFørsteMeldekortForEnSak(): Meldekort.IkkeUtfyltMeldekort {
    val periode = finnFørsteMeldekortsperiode(this.periode)
    val meldekortId = MeldekortId.random()
    val tiltakstype = this.behandling.vilkårssett.tiltakDeltagelseVilkår.registerSaksopplysning.tiltakstype
    val tiltaksnavn = this.behandling.vilkårssett.tiltakDeltagelseVilkår.registerSaksopplysning.tiltakNavn
    val utfallsperioder: Periodisering<AvklartUtfallForPeriode> = this.utfallsperioder

    return Meldekort.IkkeUtfyltMeldekort(
        id = meldekortId,
        meldeperiodeId = MeldeperiodeId.fraPeriode(periode),
        sakId = this.sakId,
        fnr = this.behandling.fnr,
        rammevedtakId = this.id,
        forrigeMeldekortId = null,
        tiltakstype = tiltakstype,
        tiltaksnavn = tiltaksnavn,
        // TODO post-mvp: Her har vi mulighet til å hente verdien fra brukers geografiske tilhørighet + norg2.
        navkontor = null,
        meldeperiode =
        Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
            meldeperiode = periode,
            utfallsperioder = utfallsperioder,
            tiltakstype = tiltakstype,
            tiltaksnavn = tiltaksnavn,
            meldekortId = meldekortId,
            sakId = this.sakId,
        ),
    )
}

fun finnFørsteMeldekortsperiode(periode: Periode): Periode {
    val førsteMandagIMeldekortsperiode = periode.fraOgMed.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sisteSøndagIMeldekortsperiode = førsteMandagIMeldekortsperiode.plusDays(13)

    return Periode(førsteMandagIMeldekortsperiode, sisteSøndagIMeldekortsperiode)
}
