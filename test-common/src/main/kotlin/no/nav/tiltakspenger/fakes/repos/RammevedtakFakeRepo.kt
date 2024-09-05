package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo

class RammevedtakFakeRepo : RammevedtakRepo {

    private val data = Atomic(mutableMapOf<VedtakId, Rammevedtak>())

    override fun hent(vedtakId: VedtakId): Rammevedtak? {
        return data.get()[vedtakId]
    }

    override fun lagreVedtak(vedtak: Rammevedtak, context: TransactionContext?) {
        data.get()[vedtak.id] = vedtak
    }

    override fun hentVedtakForIdent(ident: Fnr): List<Rammevedtak> {
        return data.get().values.filter { it.behandling.fnr == ident }
    }

    fun hentForSakId(sakId: SakId): Rammevedtak? {
        return data.get().values.find { it.behandling.sakId == sakId }
    }
}
