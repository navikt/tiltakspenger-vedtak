package no.nav.tiltakspenger.utbetaling.domene

import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.Meldeperiode
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * @property forrigeUtbetalingsvedtak er null for første utbetalingsvedtak i en sak.
 */
data class Utbetalingsvedtak(
    val id: VedtakId,
    val sakId: SakId,
    val saksnummer: Saksnummer,
    val fnr: Fnr,
    val rammevedtakId: VedtakId,
    val vedtakstidspunkt: LocalDateTime,
    val brukerNavkontor: String,
    val saksbehandler: String,
    val beslutter: String,
    val meldekortperiode: Meldeperiode.UtfyltMeldeperiode,
    val utbetalingsperiode: UtbetalingsperioderGruppertPåMeldekortperiode,
    val forrigeUtbetalingsvedtak: VedtakId?,
) {
    val periode = utbetalingsperiode.periode
    val beløp = utbetalingsperiode.beløp
    val meldekortId = utbetalingsperiode.meldekortId

    init {
        require(utbetalingsperiode.periode == utbetalingsperiode.periode) {
            "Utbetalingsperiodene må være lik meldekortperiodene"
        }
        require(vedtakstidspunkt.truncatedTo(ChronoUnit.MICROS) == vedtakstidspunkt) {
            "vedtakstidspunkt må være i mikrosekunder, men var: $vedtakstidspunkt"
        }
    }
}

/**
 * @property meldekortperiode Vi tar bare inn en periode her i MVP, siden vi ikke har støtte for korrigering av tidligere meldekort.
 */
fun Utbetalingsvedtak.nyttUtbetalingVedtak(
    saksbehandler: String,
    meldekortperiode: Meldeperiode.UtfyltMeldeperiode,
): Utbetalingsvedtak {
    return this.copy(
        id = VedtakId.random(),
        vedtakstidspunkt = LocalDateTime.now(),
        saksbehandler = saksbehandler,
        beslutter = saksbehandler,
        utbetalingsperiode = meldekortperiode.genererUtbetalingsperioderGruppertPåMeldekortperiode(),
        meldekortperiode = meldekortperiode,
        forrigeUtbetalingsvedtak = this.id,
    )
}

fun Meldekort.UtfyltMeldekort.tilUtbetalingsperiode(
    rammevedtak: Rammevedtak,
    forrigeUtbetalingsvedtak: VedtakId?,
): Utbetalingsvedtak =
    Utbetalingsvedtak(
        id = VedtakId.random(),
        sakId = this.sakId,
        saksnummer = rammevedtak.saksnummer,
        fnr = rammevedtak.fnr,
        rammevedtakId = this.rammevedtakId,
        vedtakstidspunkt = nå(),
        // TODO pre-mvp: Hent fra NORG
        brukerNavkontor = "0220",
        saksbehandler = this.saksbehandler,
        beslutter = this.beslutter!!,
        meldekortperiode = this.meldekortperiode,
        utbetalingsperiode = this.meldekortperiode.genererUtbetalingsperioderGruppertPåMeldekortperiode(),
        forrigeUtbetalingsvedtak = forrigeUtbetalingsvedtak,
    )
