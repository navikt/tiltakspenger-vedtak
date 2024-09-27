package no.nav.tiltakspenger.utbetaling.domene

import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.StatistikkUtbetalingDTO
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
    val meldekort: Meldekort.UtfyltMeldekort,
    val utbetalingsperiode: UtbetalingsperioderGruppertPåMeldekortperiode,
    val forrigeUtbetalingsvedtak: VedtakId?,
    val sendtTilUtbetaling: LocalDateTime?,
    val journalpostId: JournalpostId?,
    val journalføringstidspunkt: LocalDateTime?,
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
        meldekort = this,
        utbetalingsperiode = this.meldekortperiode.genererUtbetalingsperioderGruppertPåMeldekortperiode(),
        forrigeUtbetalingsvedtak = forrigeUtbetalingsvedtak,
        sendtTilUtbetaling = null,
        journalpostId = null,
        journalføringstidspunkt = null,
    )

fun Utbetalingsvedtak.tilStatistikk(): StatistikkUtbetalingDTO =
    StatistikkUtbetalingDTO(
        // er rammevedtakId det riktige her?
        id = this.rammevedtakId.toString(),
        sakId = this.sakId.toString(),
        saksnummer = this.saksnummer.toString(),
        beløp = this.beløp,
        beløpBeskrivelse = "",
        årsak = "",
        posteringDato = this.vedtakstidspunkt.toLocalDate(),
        gyldigFraDatoPostering = this.periode.fraOgMed,
        gyldigTilDatoPostering = this.periode.tilOgMed,
    )
