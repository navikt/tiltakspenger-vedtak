package no.nav.tiltakspenger.utbetaling.ports

import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import java.time.LocalDateTime

interface UtbetalingsvedtakRepo {
    fun lagre(vedtak: Utbetalingsvedtak, context: TransactionContext? = null)

    fun markerSendtTilUtbetaling(
        vedtakId: VedtakId,
        tidspunkt: LocalDateTime,
        utbetalingsrespons: SendtUtbetaling,
    )

    fun markerJournalført(
        vedtakId: VedtakId,
        journalpostId: JournalpostId,
        tidspunkt: LocalDateTime,
    )

    fun hentForVedtakId(vedtakId: VedtakId): Utbetalingsvedtak?

    fun hentForSakId(sakId: SakId): List<Utbetalingsvedtak>

    fun hentForFørstegangsbehandlingId(behandlingId: BehandlingId): List<Utbetalingsvedtak>

    fun hentGodkjenteMeldekortUtenUtbetalingsvedtak(limit: Int = 10): List<Meldekort.UtfyltMeldekort>

    fun hentUtbetalingsvedtakForUtsjekk(limit: Int = 10): List<Utbetalingsvedtak>

    fun hentDeSomSkalJournalføres(limit: Int = 10): List<Utbetalingsvedtak>
}
