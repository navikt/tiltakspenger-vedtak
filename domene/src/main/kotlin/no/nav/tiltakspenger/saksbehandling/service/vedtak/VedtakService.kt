package no.nav.tiltakspenger.saksbehandling.service.vedtak

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface VedtakService {
    fun hentVedtak(vedtakId: VedtakId): Vedtak?
    fun hentVedtakForBehandling(behandlingId: BehandlingId): Vedtak
}
