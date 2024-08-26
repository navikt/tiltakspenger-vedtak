package no.nav.tiltakspenger.saksbehandling.service.vedtak

import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak

interface RammevedtakService {
    fun hentVedtak(vedtakId: VedtakId): Rammevedtak?
}
