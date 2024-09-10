package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo

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

    fun hentForSakId(sakId: SakId): Rammevedtak? = data.get().values.find { it.behandling.sakId == sakId }

    fun hentForBehandlingId(behandlingId: BehandlingId): Rammevedtak? = data.get().values.find { it.behandling.id == behandlingId }
}
