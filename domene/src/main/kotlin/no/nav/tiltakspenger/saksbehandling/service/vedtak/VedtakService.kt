package no.nav.tiltakspenger.saksbehandling.service.vedtak

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface VedtakService {
    fun hentVedtak(vedtakId: VedtakId): Vedtak?

    fun hentVedtakForBehandling(behandlingId: BehandlingId): Vedtak
}
