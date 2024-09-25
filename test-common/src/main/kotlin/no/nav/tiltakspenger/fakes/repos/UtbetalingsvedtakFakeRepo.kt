package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldekort.UtfyltMeldekort
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo

class UtbetalingsvedtakFakeRepo(
    private val rammevedtakFakeRepo: RammevedtakFakeRepo,
    private val meldekortFakeRepo: MeldekortFakeRepo,
) : UtbetalingsvedtakRepo {
    private val data = Atomic(mutableMapOf<VedtakId, Utbetalingsvedtak>())

    override fun lagre(vedtak: Utbetalingsvedtak, context: TransactionContext?) {
        data.get()[vedtak.id] = vedtak
    }

    override fun markerSendtTilUtbetaling(
        vedtakId: VedtakId,
        utbetalingsrespons: SendtUtbetaling,
    ) {
        data.get()[vedtakId] = data.get()[vedtakId]!!.copy(sendtTilUtbetaling = true)
    }

    override fun markerSendtTilDokument(vedtakId: VedtakId) {
        data.get()[vedtakId] = data.get()[vedtakId]!!.copy(sendtTilDokument = true)
    }

    override fun hentForVedtakId(vedtakId: VedtakId): Utbetalingsvedtak? = data.get()[vedtakId]

    override fun hentForSakId(sakId: SakId): List<Utbetalingsvedtak> = data.get().values.filter { it.sakId == sakId }

    override fun hentForFørstegangsbehandlingId(behandlingId: BehandlingId): List<Utbetalingsvedtak> {
        val rammevedtakId = rammevedtakFakeRepo.hentForBehandlingId(behandlingId)!!.id
        return data.get().values.filter { it.rammevedtakId == rammevedtakId }
    }

    override fun hentGodkjenteMeldekortUtenUtbetalingsvedtak(limit: Int): List<UtfyltMeldekort> =
        meldekortFakeRepo.hentAlle().filter {
            data.get().values.none { utbetalingsvedtak -> utbetalingsvedtak.meldekortperiode.meldekortId == it.id }
        }

    override fun hentUtbetalingsvedtakForUtsjekk(limit: Int): List<Utbetalingsvedtak> =
        data.get().values.filter { it.sendtTilUtbetaling }

    override fun hentUtbetalingsvedtakForDokument(limit: Int): List<Utbetalingsvedtak> =
        data.get().values.filter { it.sendtTilDokument }
}
