package no.nav.tiltakspenger.utbetaling.ports

import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.Utbetalinger
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

    fun hentUtbetalingJsonForVedtakId(vedtakId: VedtakId): String?

    fun hentForFørstegangsbehandlingId(behandlingId: BehandlingId): Utbetalinger

    fun hentUtbetalingsvedtakForUtsjekk(limit: Int = 10): List<Utbetalingsvedtak>

    fun hentDeSomSkalJournalføres(limit: Int = 10): List<Utbetalingsvedtak>
}
