package no.nav.tiltakspenger.meldekort.domene

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.tiltakspenger.felles.Saksbehandler
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
import java.time.temporal.TemporalAdjusters

sealed interface Meldekort {
    val id: MeldekortId
    val sakId: SakId
    val rammevedtakId: VedtakId
    val forrigeMeldekortId: MeldekortId?
    val meldekortperiode: Meldeperiode
    val tiltakstype: TiltakstypeSomGirRett
    val fraOgMed: LocalDate get() = meldekortperiode.fraOgMed
    val tilOgMed: LocalDate get() = meldekortperiode.tilOgMed
    val periode: Periode get() = meldekortperiode.periode
    val saksbehandler: String?
    val beslutter: String?

    /**
     * Meldekort utfylt av saksbehandler og godkjent av beslutter.
     * Når veileder/bruker har fylt ut meldekortet vil ikke denne klassen kunne gjenbrukes uten endringer. Kanskje vi må ha en egen klasse for veileder-/brukerutfylt meldekort.
     *
     * @param saksbehandler: Obligatorisk dersom meldekortet er utfylt av saksbehandler.
     * @param beslutter: Obligatorisk dersom meldekortet er godkjent av beslutter.
     *
     * TODO pre-mvp jah: Verifiser at saksbehandler og beslutter ikke er den samme.
     */
    data class UtfyltMeldekort(
        override val id: MeldekortId,
        override val sakId: SakId,
        override val rammevedtakId: VedtakId,
        override val forrigeMeldekortId: MeldekortId?,
        override val meldekortperiode: Meldeperiode.UtfyltMeldeperiode,
        override val tiltakstype: TiltakstypeSomGirRett,
        override val saksbehandler: String,
        override val beslutter: String?,
    ) : Meldekort {
        /**
         * TODO post-mvp jah: Ved revurderinger av rammevedtaket, så må vi basere oss på både forrige meldekort og revurderingsvedtaket. Dette løser vi å flytte mer logikk til Sak.kt.
         * TODO post-mvp jah: Når vi implementerer delvis innvilgelse vil hele meldekortperioder bli SPERRET.
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
                sakId = this.sakId,
                rammevedtakId = this.rammevedtakId,
                forrigeMeldekortId = this.id,
                tiltakstype = this.tiltakstype,
                meldekortperiode =
                Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
                    meldeperiode = periode,
                    tiltakstype = this.tiltakstype,
                    meldekortId = meldekortId,
                    sakId = this.sakId,
                    utfallsperioder = utfallsperioder,
                ),
            ).right()
        }

        fun iverksettMeldekort(beslutter: Saksbehandler): UtfyltMeldekort =
            UtfyltMeldekort(
                id = this.id,
                sakId = this.sakId,
                rammevedtakId = this.rammevedtakId,
                forrigeMeldekortId = this.forrigeMeldekortId,
                meldekortperiode = this.meldekortperiode,
                tiltakstype = this.tiltakstype,
                saksbehandler = this.saksbehandler,
                beslutter = beslutter.navIdent,
            )
    }

    data class IkkeUtfyltMeldekort(
        override val id: MeldekortId,
        override val sakId: SakId,
        override val rammevedtakId: VedtakId,
        override val forrigeMeldekortId: MeldekortId?,
        override val tiltakstype: TiltakstypeSomGirRett,
        override val meldekortperiode: Meldeperiode.IkkeUtfyltMeldeperiode,
    ) : Meldekort {

        fun sendTilBeslutter(
            meldekortperiode: Meldeperiode.UtfyltMeldeperiode,
            saksbehandler: Saksbehandler,
        ): Either<KanIkkeSendeMeldekortTilBeslutter, UtfyltMeldekort> {
            if (LocalDate.now().isBefore(meldekortperiode.periode.fraOgMed)) {
                // John har avklart med Sølvi og Taulant at vi bør ha en begrensning på at vi kan fylle ut et meldekort hvis dagens dato er innenfor meldekortperioden eller senere.
                // Dette kan endres på ved behov.
                return KanIkkeSendeMeldekortTilBeslutter.MeldekortperiodenKanIkkeVæreFremITid.left()
            }
            return UtfyltMeldekort(
                id = this.id,
                sakId = this.sakId,
                rammevedtakId = this.rammevedtakId,
                forrigeMeldekortId = this.forrigeMeldekortId,
                meldekortperiode = meldekortperiode,
                tiltakstype = this.tiltakstype,
                saksbehandler = saksbehandler.navIdent,
                beslutter = this.beslutter,
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
    val utfallsperioder: Periodisering<AvklartUtfallForPeriode> = this.utfallsperioder

    return Meldekort.IkkeUtfyltMeldekort(
        id = meldekortId,
        sakId = this.sakId,
        rammevedtakId = this.id,
        forrigeMeldekortId = null,
        tiltakstype = tiltakstype,
        meldekortperiode =
        Meldeperiode.IkkeUtfyltMeldeperiode.fraPeriode(
            meldeperiode = periode,
            utfallsperioder = utfallsperioder,
            tiltakstype = tiltakstype,
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
