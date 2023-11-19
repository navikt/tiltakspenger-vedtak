package no.nav.tiltakspenger.vedtak.repository.vedtak

import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId

interface VedtakRepo {
    fun hent(vedtakId: VedtakId): Vedtak?
    fun hentVedtakForBehandling(behandlingId: BehandlingId): List<Vedtak>
    fun lagreVedtak(vedtak: Vedtak): Vedtak
}
