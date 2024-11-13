package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.distribusjon.domene.DistribusjonId
import no.nav.tiltakspenger.distribusjon.domene.VedtakSomSkalDistribueres
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import java.time.LocalDateTime

class RammevedtakFakeRepo : RammevedtakRepo {

    private val data = Atomic(mutableMapOf<VedtakId, Rammevedtak>())

    override fun hentForVedtakId(vedtakId: VedtakId): Rammevedtak? = data.get()[vedtakId]

    override fun lagre(
        vedtak: Rammevedtak,
        context: TransactionContext?,
    ) {
        data.get()[vedtak.id] = vedtak
    }

    override fun hentForFnr(fnr: Fnr): List<Rammevedtak> = data.get().values.filter { it.behandling.fnr == fnr }

    override fun hentRammevedtakSomSkalJournalføres(limit: Int): List<Rammevedtak> {
        return data.get().values.filter { it.journalpostId == null }.take(limit)
    }

    override fun markerJournalført(id: VedtakId, journalpostId: JournalpostId, tidspunkt: LocalDateTime) {
        data.get()[id] = data.get()[id]!!.copy(journalpostId = journalpostId, journalføringstidstpunkt = tidspunkt)
    }

    override fun hentRammevedtakSomSkalDistribueres(limit: Int): List<VedtakSomSkalDistribueres> {
        return data.get().values.filter { it.distribusjonId == null }.map {
            VedtakSomSkalDistribueres(it.id, it.journalpostId!!)
        }.take(limit)
    }

    override fun markerDistribuert(id: VedtakId, distribusjonId: DistribusjonId, tidspunkt: LocalDateTime) {
        data.get()[id] = data.get()[id]!!.copy(distribusjonId = distribusjonId, distribusjonstidspunkt = tidspunkt)
    }

    override fun hentRammevedtakTilDatadeling(limit: Int): List<Rammevedtak> {
        return data.get().values.filter { it.sendtTilDatadeling == null }.take(limit)
    }

    override fun markerSendtTilDatadeling(id: VedtakId, tidspunkt: LocalDateTime) {
        TODO("Not yet implemented")
    }

    fun hentForSakId(sakId: SakId): Rammevedtak? = data.get().values.find { it.behandling.sakId == sakId }

    fun hentForBehandlingId(behandlingId: BehandlingId): Rammevedtak? =
        data.get().values.find { it.behandling.id == behandlingId }
}
