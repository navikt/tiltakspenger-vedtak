package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface VedtakRepo {
    fun hent(vedtakId: VedtakId): Vedtak?
    fun hentVedtakForBehandling(behandlingId: BehandlingId): Vedtak
    fun lagreVedtak(vedtak: Vedtak, context: TransactionContext? = null): Vedtak
}
