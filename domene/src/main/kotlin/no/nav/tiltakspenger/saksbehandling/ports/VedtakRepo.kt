package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface VedtakRepo {
    fun hent(vedtakId: VedtakId): Vedtak?

    fun hentVedtakForBehandling(behandlingId: BehandlingId): Vedtak

    fun lagreVedtak(
        vedtak: Vedtak,
        context: TransactionContext? = null,
    ): Vedtak

    fun hentVedtakSomIkkeErSendtTilMeldekort(limit: Int = 10): List<Vedtak>

    fun oppdaterVedtakSendtTilMeldekort(id: VedtakId)
}
